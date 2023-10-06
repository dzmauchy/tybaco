package org.tybaco.runtime.logging;

/*-
 * #%L
 * runtime
 * %%
 * Copyright (C) 2023 Montoni
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.SLF4JServiceProvider;
import org.tybaco.runtime.util.IO;

import java.io.OutputStream;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.System.Logger.Level.valueOf;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.tybaco.runtime.util.Settings.intSetting;
import static org.tybaco.runtime.util.Settings.sizeSetting;

public final class LoggingServiceProvider implements SLF4JServiceProvider, AutoCloseable {

  final HashMap<String, System.Logger.Level> markerFilters = new HashMap<>(16, 0.5f);
  final HashMap<Pattern, System.Logger.Level> patternFilters = new HashMap<>(16, 0.5f);

  private final ReferenceQueue<Logger> referenceQueue = new ReferenceQueue<>();
  private final FastMarkerFactory markerFactory = new FastMarkerFactory();
  private final FastMDCAdapter mdcAdapter = new FastMDCAdapter();
  private final HostContext hostContext = new HostContext();
  private final ConcurrentHashMap<String, LoggerRef> loggers = new ConcurrentHashMap<>(128, 0.5f);
  private final OutputStream outputStream;
  private final ArrayBlockingQueue<LogRecord> queue;
  private final LogRecordBuffer recordBuffer;
  private final FileBuffer buffer;
  private final Thread logThread;

  private volatile boolean running = true;

  public LoggingServiceProvider() {
    this(System.out);
  }

  public LoggingServiceProvider(OutputStream outputStream) {
    this.outputStream = outputStream;
    this.recordBuffer = new LogRecordBuffer(intSetting("TY_LOG_QUEUE_SIZE").orElse(64));
    this.queue = new ArrayBlockingQueue<>(recordBuffer.maxSize(), true);
    this.buffer = new FileBuffer(sizeSetting("TY_MAX_LOG_RECORD_SIZE").orElse(1 << 20));
    this.logThread = new Thread(this::run, "__LOG__");
    this.logThread.setDaemon(true);
    if (outputStream != System.out) initialize();
  }

  @Override
  public ILoggerFactory getLoggerFactory() {
    return name -> {
      var ref = new AtomicReference<Logger>();
      loggers.compute(name, (k, o) -> {
        if (o == null) {
          clean();
          var l = new Logger(this, k);
          ref.set(l);
          return new LoggerRef(l, referenceQueue);
        } else {
          var l = o.get();
          if (l == null) {
            clean();
            l = new Logger(this, k);
            ref.set(l);
            return new LoggerRef(l, referenceQueue);
          } else {
            ref.set(l);
            return o;
          }
        }
      });
      return ref.get();
    };
  }

  private void clean() {
    while (true) {
      var ref = referenceQueue.poll();
      if (ref == null) break;
      if (ref instanceof LoggerRef r) {
        loggers.remove(r.name);
      }
    }
  }

  @Override
  public FastMarkerFactory getMarkerFactory() {
    return markerFactory;
  }

  @Override
  public FastMDCAdapter getMDCAdapter() {
    return mdcAdapter;
  }

  @Override
  public String getRequestedApiVersion() {
    return "2.0.99";
  }

  private void run() {
    while (running) {
      try {
        processRecord();
      } catch (Throwable e) {
        e.printStackTrace(System.err);
      }
    }
    try {
      drain();
    } catch (Throwable e) {
      e.printStackTrace(System.err);
    }
  }

  private void processRecord() {
    if (!drain()) {
      try {
        clean();
        var r = queue.poll(10L, MILLISECONDS);
        if (r != null) log(r);
      } catch (InterruptedException e) {
        e.printStackTrace(System.err);
      }
    }
  }

  private boolean drain() {
    var count = queue.drainTo(recordBuffer, MAX_VALUE);
    if (count > 0) {
      try {
        recordBuffer.forEach(this::log);
      } finally {
        recordBuffer.reset();
      }
      return true;
    } else {
      return false;
    }
  }

  void put(LogRecord record) {
    if (!queue.offer(record)) {
      while (running) {
        try {
          if (queue.offer(record, 10L, MILLISECONDS)) return;
        } catch (Throwable e) {
          e.printStackTrace(System.err);
          return;
        }
      }
      synchronized (this) {
        log(record);
      }
    }
  }

  @Override
  public void initialize() {
    if (outputStream == System.out) {
      System.setErr(new LoggingStream(queue, mdcAdapter));
    }
    var classLoader = Thread.currentThread().getContextClassLoader();
    classLoader.resources("tybaco/logging.properties").forEach(url -> {
      var properties = IO.loadProperties(url);
      properties.forEach((ko, vo) -> {
        if (ko instanceof String k && vo instanceof String v) {
          if (k.startsWith("pattern.")) {
            var idx = v.indexOf(',');
            if (idx >= 0) {
              try {
                var level = valueOf(v.substring(0, idx));
                var pattern = Pattern.compile(v.substring(idx + 1));
                patternFilters.put(pattern, level);
              } catch (Throwable x) {
                throw new IllegalStateException("Unable to process " + k + " of " + url, x);
              }
            }
          } else if (k.startsWith("marker.")) {
            try {
              var level = valueOf(v);
              var marker = k.substring("marker.".length());
              markerFilters.put(marker, level);
            } catch (Throwable x) {
              throw new IllegalStateException("Unable to process " + k + " of " + url, x);
            }
          }
        }
      });
    });
    logThread.start();
  }

  @Override
  public void close() {
    running = false;
    try (buffer) {
      logThread.join();
      synchronized (this) {
        drain();
      }
    } catch (Throwable e) {
      e.printStackTrace(System.err);
    }
  }

  private void log(LogRecord record) {
    try {
      record.writeTo(buffer, hostContext);
      buffer.rewind(outputStream);
      outputStream.flush();
    } catch (Throwable e) {
      e.printStackTrace(System.err);
    } finally {
      buffer.reset();
    }
  }

  private static final class LoggerRef extends WeakReference<Logger> {

    private final String name;

    public LoggerRef(Logger referent, ReferenceQueue<Logger> queue) {
      super(referent, queue);
      this.name = referent.getName();
    }
  }
}
