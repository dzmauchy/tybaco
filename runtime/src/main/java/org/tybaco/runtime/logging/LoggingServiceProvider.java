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
import org.slf4j.helpers.*;
import org.slf4j.spi.SLF4JServiceProvider;

import java.io.*;
import java.util.IdentityHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

import static java.util.Objects.requireNonNullElse;
import static org.slf4j.helpers.MessageFormatter.arrayFormat;

public final class LoggingServiceProvider implements SLF4JServiceProvider {

  private static final int MAX_MARKER_LENGTH = 1024;
  static volatile boolean initialized;

  private final SynchronousQueue<LogRecord> queue = new SynchronousQueue<>(true);
  private final FastMarkerFactory markerFactory = new FastMarkerFactory();
  private final FastMDCAdapter mdcAdapter = new FastMDCAdapter();
  private final ConcurrentHashMap<String, Logger> loggers = new ConcurrentHashMap<>(128, 0.5f);
  private final PrintStream outputStream = System.err;
  private final DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
  private final Thread logThread = new Thread(this::run, "_LOG_");
  private final LogFactory logFactory = new LogFactory();

  public LoggingServiceProvider() {
    logThread.setDaemon(true);
  }

  @Override
  public ILoggerFactory getLoggerFactory() {
    return logFactory;
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
      processRecord();
    }
  }

  private void processRecord() {
    try {
      var rec = queue.take();
      log(rec.level(), rec.marker(), arrayFormat(rec.msg(), rec.args(), rec.throwable()));
    } catch (Throwable ignore) {
    }
  }

  @Override
  public void initialize() {
    System.setErr(new LoggingErrorStream());
    logThread.start();
    initialized = true;
  }

  private boolean writeMarkers(Marker marker) throws IOException {
    if (marker == null) {
      dataOutputStream.write(0);
      return false;
    }
    var name = marker.getName();
    if (name.length() > MAX_MARKER_LENGTH) {
      dataOutputStream.write(0);
      return false;
    }
    dataOutputStream.write(1);
    dataOutputStream.writeUTF(name);
    for (var it = marker.iterator(); it.hasNext(); ) {
      writeMarkers(it.next());
    }
    return true;
  }

  private void log(Level level, Marker marker, FormattingTuple tuple) {
    try {
      var msg = tuple.getMessage();
      synchronized (this) {
        dataOutputStream.writeInt(level.toInt());
        if (writeMarkers(marker)) dataOutputStream.write(0);
        dataOutputStream.writeInt(msg == null ? 0 : msg.length());
        if (msg != null) dataOutputStream.writeChars(msg);
        writeThrowable(tuple.getThrowable(), new IdentityHashMap<>(8));
        mdcAdapter.map.get().forEach((k, v) -> {
          if (k != null && v != null && k.length() <= MAX_MARKER_LENGTH && v.length() <= MAX_MARKER_LENGTH) {
            try {
              dataOutputStream.write(1);
              dataOutputStream.writeUTF(k);
              dataOutputStream.writeUTF(v);
            } catch (Throwable ignore) {
            }
          }
        });
        dataOutputStream.write(0);
        mdcAdapter.deques.get().forEach((k, q) -> {
          if (k != null && k.length() <= MAX_MARKER_LENGTH) {
            try {
              dataOutputStream.write(1);
              dataOutputStream.writeUTF(k);
              for (var v : q) {
                if (v != null && v.length() <= MAX_MARKER_LENGTH) {
                  dataOutputStream.write(1);
                  dataOutputStream.writeUTF(v);
                }
              }
              dataOutputStream.write(0);
            } catch (Throwable ignore) {
            }
          }
        });
        dataOutputStream.write(0);
        outputStream.flush();
      }
    } catch (Throwable ignore) {
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
    var suppressed = t.getSuppressed();
    dataOutputStream.writeInt(suppressed.length);
    for (var s : suppressed) writeThrowable(s, passed);
    var st = t.getStackTrace();
    dataOutputStream.writeInt(st.length);
    for (var s : st) {
      dataOutputStream.writeUTF(requireNonNullElse(s.getClassLoaderName(), ""));
      dataOutputStream.writeUTF(requireNonNullElse(s.getModuleName(), ""));
      dataOutputStream.writeUTF(requireNonNullElse(s.getModuleVersion(), ""));
      dataOutputStream.writeUTF(s.getClassName());
      dataOutputStream.writeUTF(requireNonNullElse(s.getFileName(), ""));
      dataOutputStream.writeUTF(s.getMethodName());
      dataOutputStream.writeBoolean(s.isNativeMethod());
      dataOutputStream.writeInt(s.getLineNumber());
    }
  }

  final class LogFactory implements ILoggerFactory {

    @Override
    public Logger getLogger(String name) {
      return loggers.computeIfAbsent(name, StdLogger::new);
    }

    SynchronousQueue<LogRecord> queue() {
      return queue;
    }
  }

  private final class StdLogger extends AbstractLogger {

    private final String name;

    private StdLogger(String name) {
      this.name = name;
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
        queue.put(new LogRecord(level, marker, messagePattern, arguments, throwable));
      } catch (Throwable ignore) {
      }
    }

    @Override
    public boolean isTraceEnabled() {
      return true;
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
      return true;
    }

    @Override
    public boolean isDebugEnabled() {
      return true;
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
      return true;
    }

    @Override
    public boolean isInfoEnabled() {
      return true;
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
      return true;
    }

    @Override
    public boolean isWarnEnabled() {
      return true;
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
      return true;
    }

    @Override
    public boolean isErrorEnabled() {
      return true;
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
      return true;
    }
  }
}
