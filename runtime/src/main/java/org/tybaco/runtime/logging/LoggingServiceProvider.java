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
import java.util.IdentityHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

import static java.util.Objects.requireNonNullElse;

public final class LoggingServiceProvider implements SLF4JServiceProvider {

  static volatile boolean initialized;

  private final SynchronousQueue<LogRecord> queue = new SynchronousQueue<>(true);
  private final FastMarkerFactory markerFactory = new FastMarkerFactory();
  private final FastMDCAdapter mdcAdapter = new FastMDCAdapter();
  private final ConcurrentHashMap<String, Logger> loggers = new ConcurrentHashMap<>(128, 0.5f);
  private final PrintStream outputStream;
  private final DataOutputStream dataOutputStream;
  private final Thread logThread;
  private final LogFactory logFactory;

  public LoggingServiceProvider() {
    this(System.err);
  }

  public LoggingServiceProvider(PrintStream outputStream) {
    this.outputStream = outputStream;
    this.dataOutputStream = new DataOutputStream(outputStream);
    this.logThread = new Thread(this::run, "__LOG__");
    this.logFactory = new LogFactory();
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
      log(queue.take());
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
      outputStream.write(0);
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
        var thread = Thread.currentThread();
        var time = System.currentTimeMillis();
        queue.put(new LogRecord(level, thread, time, name, marker, messagePattern, arguments, throwable));
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
