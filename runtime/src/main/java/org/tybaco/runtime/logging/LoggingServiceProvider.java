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

import org.slf4j.*;
import org.slf4j.event.Level;
import org.slf4j.helpers.AbstractLogger;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.SLF4JServiceProvider;

import java.io.*;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNullElse;
import static org.tybaco.runtime.util.Settings.intSetting;

public final class LoggingServiceProvider implements SLF4JServiceProvider, AutoCloseable {

  final HashMap<String, System.Logger.Level> markerFilters = new HashMap<>(16, 0.5f);
  final HashMap<Pattern, System.Logger.Level> patternFilters = new HashMap<>(16, 0.5f);

  private final ReferenceQueue<StdLogger> referenceQueue = new ReferenceQueue<>();
  private final int queueSize = intSetting("TY_LOG_QUEUE_SIZE").orElse(64);
  private final ArrayBlockingQueue<LogRecord> queue = new ArrayBlockingQueue<>(queueSize, true);
  private final FastMarkerFactory markerFactory = new FastMarkerFactory();
  private final FastMDCAdapter mdcAdapter = new FastMDCAdapter();
  private final ConcurrentHashMap<String, LoggerRef> loggers = new ConcurrentHashMap<>(128, 0.5f);
  private final OutputStream outputStream;
  private final DataOutputStream dataOutputStream;
  private final Thread logThread;

  public LoggingServiceProvider() {
    this(System.err);
  }

  public LoggingServiceProvider(OutputStream outputStream) {
    this.outputStream = outputStream;
    this.dataOutputStream = new DataOutputStream(outputStream);
    this.logThread = new Thread(this::run, "__LOG__");
    logThread.setDaemon(true);
  }

  @Override
  public ILoggerFactory getLoggerFactory() {
    return name -> {
      var ref = new AtomicReference<StdLogger>();
      loggers.compute(name, (k, o) -> {
        if (o == null) {
          clean();
          var l = new StdLogger(k);
          ref.set(l);
          return new LoggerRef(l, referenceQueue);
        } else {
          var l = o.get();
          if (l == null) {
            clean();
            l = new StdLogger(k);
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
    while (true) {
      try {
        processRecord();
      } catch (InterruptedException ignore) {
        logThread.interrupt();
        break;
      } catch (Throwable ignore) {
      }
    }
  }

  private void processRecord() throws InterruptedException {
    var array = new ArrayList<LogRecord>(queueSize);
    var count = queue.drainTo(array, queueSize);
    if (count > 0) {
      array.forEach(this::log);
      if (logThread.isInterrupted()) throw new InterruptedException();
    } else {
      clean();
      log(queue.take());
    }
  }

  @Override
  public void initialize() {
    if (outputStream == System.err) {
      var initialized = new AtomicBoolean();
      var errorStream = new LoggingErrorStream(queue, initialized);
      System.setErr(errorStream);
      Thread.startVirtualThread(() -> {
        var f = LoggerFactory.getILoggerFactory();
        initialized.set(f != null);
      });
      var classLoader = Thread.currentThread().getContextClassLoader();
      try {
        for (var e = classLoader.getResources("tybaco/logging.properties"); e.hasMoreElements(); ) {
          var url = e.nextElement();
          var properties = new Properties();
          try {
            try (var is = url.openStream(); var r = new InputStreamReader(is)) {
              properties.load(r);
            }
            properties.forEach((ko, vo) -> {
              if (ko instanceof String k && vo instanceof String v) {
                if (k.startsWith("pattern.")) {
                  var idx = v.indexOf(',');
                  if (idx >= 0) {
                    try {
                      var level = System.Logger.Level.valueOf(v.substring(0, idx));
                      var pattern = Pattern.compile(v.substring(idx + 1));
                      patternFilters.put(pattern, level);
                    } catch (Throwable x) {
                      throw new IllegalStateException("Unable to process " + k + " of " + url, x);
                    }
                  }
                } else if (k.startsWith("marker.")) {
                  try {
                    var level = System.Logger.Level.valueOf(v);
                    var marker = k.substring("marker.".length());
                    markerFilters.put(marker, level);
                  } catch (Throwable x) {
                    throw new IllegalStateException("Unable to process " + k + " of " + url, x);
                  }
                }
              }
            });
          } catch (IOException x) {
            throw new UncheckedIOException("Unable to process " + url, x);
          }
        }
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    logThread.start();
  }

  @Override
  public void close() throws Exception {
    logThread.interrupt();
    logThread.join();
  }

  private boolean writeMarkers(Marker marker) throws IOException {
    if (marker == null) {
      dataOutputStream.write(0);
      return false;
    }
    dataOutputStream.write(1);
    dataOutputStream.writeUTF(marker.getName());
    for (var it = marker.iterator(); it.hasNext(); ) {
      writeMarkers(it.next());
    }
    return true;
  }

  private int threadState(Thread thread) {
    var state = 0;
    if (thread.isDaemon()) state |= 0x01;
    if (thread.isInterrupted()) state |= 0x02;
    if (thread.isVirtual()) state |= 0x04;
    return state;
  }

  private void writeThreadInfo(Thread thread) throws IOException {
    var group = thread.getThreadGroup();
    dataOutputStream.writeUTF(group == null ? "" : requireNonNullElse(group.getName(), ""));
    dataOutputStream.writeUTF(requireNonNullElse(thread.getName(), ""));
    dataOutputStream.writeLong(thread.threadId());
    dataOutputStream.writeInt(thread.getPriority());
    outputStream.write(threadState(thread));
  }

  private void log(LogRecord record) {
    try {
      var tuple = MessageFormatter.arrayFormat(record.msg(), record.args(), record.throwable());
      var msg = requireNonNullElse(tuple.getMessage(), "");
      outputStream.write(0); // protocol
      outputStream.write(record.level().toInt());
      dataOutputStream.writeUTF(record.logger());
      dataOutputStream.writeLong(record.time());
      writeThreadInfo(record.thread());
      if (writeMarkers(record.marker())) outputStream.write(0);
      dataOutputStream.writeInt(msg.length());
      dataOutputStream.writeChars(msg);
      writeThrowable(tuple.getThrowable());
      mdcAdapter.map.get().forEach((k, v) -> {
        try {
          outputStream.write(1);
          dataOutputStream.writeUTF(k);
          dataOutputStream.writeUTF(v);
        } catch (Throwable ignore) {
        }
      });
      outputStream.write(0);
      mdcAdapter.queues.get().forEach((k, q) -> {
        try {
          outputStream.write(1);
          dataOutputStream.writeUTF(k);
          for (var v : q) {
            outputStream.write(1);
            dataOutputStream.writeUTF(v);
          }
          dataOutputStream.write(0);
        } catch (Throwable ignore) {
        }
      });
      outputStream.write(0);
      outputStream.flush();
    } catch (Throwable ignore) {
    }
  }

  private void writeThrowable(Throwable t) throws IOException {
    if (t == null) {
      outputStream.write(0);
    } else {
      writeThrowable(t, new IdentityHashMap<>(8));
    }
  }

  private void writeThrowable(Throwable t, IdentityHashMap<Throwable, Boolean> passed) throws IOException {
    if (t == null || passed.put(t, Boolean.TRUE) != null) {
      outputStream.write(0);
      return;
    }
    outputStream.write(1);
    dataOutputStream.writeUTF(t.getClass().getName());
    var msg = requireNonNullElse(t.getMessage(), "");
    outputStream.write(msg.length());
    dataOutputStream.writeChars(msg);
    writeThrowable(t.getCause(), passed);
    for (var s : t.getSuppressed()) {
      outputStream.write(1);
      writeThrowable(s, passed);
    }
    outputStream.write(0);
    for (var s : t.getStackTrace()) {
      outputStream.write(1);
      dataOutputStream.writeUTF(requireNonNullElse(s.getClassLoaderName(), ""));
      dataOutputStream.writeUTF(requireNonNullElse(s.getModuleName(), ""));
      dataOutputStream.writeUTF(requireNonNullElse(s.getModuleVersion(), ""));
      dataOutputStream.writeUTF(s.getClassName());
      dataOutputStream.writeUTF(requireNonNullElse(s.getFileName(), ""));
      dataOutputStream.writeUTF(s.getMethodName());
      dataOutputStream.writeInt(s.getLineNumber());
    }
    outputStream.write(1);
  }

  private final class StdLogger extends AbstractLogger {

    private final int effectiveLevel;

    private StdLogger(String name) {
      this.name = name;
      this.effectiveLevel = patternFilters.entrySet().parallelStream()
        .filter(p -> p.getKey().matcher(name).matches())
        .mapToInt(e -> e.getValue().getSeverity())
        .max()
        .orElseGet(System.Logger.Level.ALL::getSeverity);
    }

    @Override
    public String toString() {
      return name;
    }

    @Override
    protected String getFullyQualifiedCallerName() {
      return null;
    }

    @Override
    protected void handleNormalizedLoggingCall(Level level, Marker marker, String messagePattern, Object[] arguments, Throwable throwable) {
      try {
        var thread = Thread.currentThread();
        var time = System.currentTimeMillis();
        queue.put(new LogRecord(level, thread, time, name, marker, messagePattern, arguments, throwable));
      } catch (Throwable ignore) {
      }
    }

    @Override
    public boolean isTraceEnabled() {
      return levelEnabled(System.Logger.Level.TRACE);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
      return markerEnabled(System.Logger.Level.TRACE, marker) && levelEnabled(System.Logger.Level.TRACE);
    }

    @Override
    public boolean isDebugEnabled() {
      return levelEnabled(System.Logger.Level.DEBUG);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
      return markerEnabled(System.Logger.Level.DEBUG, marker) && levelEnabled(System.Logger.Level.DEBUG);
    }

    @Override
    public boolean isInfoEnabled() {
      return levelEnabled(System.Logger.Level.INFO);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
      return markerEnabled(System.Logger.Level.INFO, marker) && levelEnabled(System.Logger.Level.INFO);
    }

    @Override
    public boolean isWarnEnabled() {
      return levelEnabled(System.Logger.Level.WARNING);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
      return markerEnabled(System.Logger.Level.WARNING, marker) && levelEnabled(System.Logger.Level.WARNING);
    }

    @Override
    public boolean isErrorEnabled() {
      return levelEnabled(System.Logger.Level.ERROR);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
      return markerEnabled(System.Logger.Level.ERROR, marker) && levelEnabled(System.Logger.Level.ERROR);
    }

    private boolean markerEnabled(System.Logger.Level level, Marker marker) {
      var l = markerFilters.get(marker.getName());
      if (l != null && level.getSeverity() < l.getSeverity()) return false;
      for (var it = marker.iterator(); it.hasNext(); ) {
        if (!markerEnabled(level, it.next())) return false;
      }
      return true;
    }

    private boolean levelEnabled(System.Logger.Level level) {
      return level.getSeverity() >= effectiveLevel;
    }
  }

  private static final class LoggerRef extends WeakReference<StdLogger> {

    private final String name;

    public LoggerRef(StdLogger referent, ReferenceQueue<StdLogger> queue) {
      super(referent, queue);
      this.name = referent.getName();
    }
  }
}
