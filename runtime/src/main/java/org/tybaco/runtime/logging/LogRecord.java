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

import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.MessageFormatter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.*;

record LogRecord(
  Level level,
  Thread thread,
  Instant time,
  String logger,
  Marker marker,
  String msg,
  Throwable throwable,
  TreeMap<String, String> mdc,
  TreeMap<String, LinkedList<String>> stack,
  String stacktrace
) {

  LogRecord(
    Level level,
    Thread thread,
    Instant time,
    String logger,
    Marker marker,
    String fmt,
    Object[] args,
    Throwable error,
    FastMDCAdapter mdc
  ) {
    this(
      level,
      thread,
      time,
      logger,
      marker,
      msg(fmt, args, error),
      error,
      mdc.map.get(),
      mdc.queues.get(),
      stacktrace(error)
    );
  }

  void writeTo(FileBuffer buffer, HostContext context) {
    buffer.write('{');
    writeBody(buffer, context);
    buffer.write('}');
    buffer.write('\n');
  }

  private void writeBody(FileBuffer buffer, HostContext context) {
    writeTimestamp(buffer);
    writeLogLevel(buffer);
    writeLogger(buffer);
    writeMessage(buffer);
    writeUser(buffer, context);
    writeLabels(buffer);
    writeError(buffer);
    writePid(buffer, context);
  }

  private void writeTimestamp(FileBuffer buffer) {
    buffer.writeSafePair("@timestamp", time.toString());
    buffer.write(',');
  }

  private void writeLogLevel(FileBuffer buffer) {
    buffer.writeSafePair("log.level", level.toString());
    buffer.write(',');
  }

  private void writeLogger(FileBuffer buffer) {
    buffer.writePair("log.logger", logger);
    buffer.write(',');
  }

  private void writeMessage(FileBuffer buffer) {
    buffer.writePair("message", msg);
    buffer.write(',');
  }

  private void writeUser(FileBuffer buffer, HostContext context) {
    buffer.writePair("user.name", context.users.getFirst());
    buffer.write(',');
  }

  private void writeLabels(FileBuffer buffer) {
    var it = mdc.entrySet().iterator();
    if (it.hasNext()) {
      buffer.writeKey("labels");
      buffer.write(':');
      buffer.write('{');
      while (it.hasNext()) {
        var e = it.next();
        buffer.writeKey(e.getKey());
        buffer.write(':');
        buffer.writeQuotedString(e.getValue());
        if (it.hasNext()) {
          buffer.write(',');
        }
      }
      buffer.write('}');
      buffer.write(',');
    }
  }

  private void writeError(FileBuffer buffer) {
    if (stacktrace != null) {
      buffer.writePair("error.stack_trace", stacktrace);
      buffer.write(',');
    }
  }

  private void writePid(FileBuffer buffer, HostContext context) {
    buffer.writePair("process.pid", context.pid);
  }

  private static String msg(String fmt, Object[] args, Throwable e) {
    var tuple = MessageFormatter.arrayFormat(fmt, args, e);
    return tuple.getMessage();
  }

  private static String stacktrace(Throwable error) {
    if (error == null) {
      return null;
    } else {
      var w = new StringWriter();
      try (var pw = new PrintWriter(w)) {
        error.printStackTrace(pw);
      }
      return w.toString();
    }
  }
}
