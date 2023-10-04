package org.tybaco.common.logging;

/*-
 * #%L
 * common
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

import java.io.*;
import java.util.*;

import static java.util.Collections.unmodifiableNavigableMap;
import static java.util.Collections.unmodifiableNavigableSet;

public final class LogStream {

  private final DataInputStream is;

  public LogStream(InputStream is) {
    this.is = new DataInputStream(is);
  }

  public LogStream(DataInputStream is) {
    this.is = is;
  }

  public LogRecord nextRecord() {
    try {
      var protocol = is.read();
      if (protocol != 0) throw new StreamCorruptedException("Invalid protocol: " + protocol);
      var level = level(is.read());
      var logger = is.readUTF();
      var time = is.readLong();
      var threadInfo = threadInfo();
      var markers = markers();
      var msg = message();
      var error = error();
      var ctx = ctx();
      var queues = queues();
      var context = new LogContext(markers, ctx, queues);
      return new LogRecord(level, time, logger, threadInfo, msg, context, error);
    } catch (EOFException ignore) {
      return null;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private String message() throws IOException {
    var msgChars = new char[is.readInt()];
    if (msgChars.length == 0) return "";
    for (int i = 0; i < msgChars.length; i++) msgChars[i] = is.readChar();
    return new String(msgChars);
  }

  private NavigableSet<String> markers() throws IOException {
    var map = new TreeMap<String, Boolean>();
    while (is.read() != 0) {
      map.put(is.readUTF(), Boolean.TRUE);
    }
    return unmodifiableNavigableSet(map.navigableKeySet());
  }

  private NavigableMap<String, String> ctx() throws IOException {
    var map = new TreeMap<String, String>();
    while (is.read() != 0) {
      var k = is.readUTF();
      var v = is.readUTF();
      map.put(k, v);
    }
    return unmodifiableNavigableMap(map);
  }

  private NavigableMap<String, List<String>> queues() throws IOException {
    var map = new TreeMap<String, List<String>>();
    while (is.read() != 0) {
      var k = is.readUTF();
      var l = map.computeIfAbsent(k, key -> new LinkedList<>());
      while (is.read() != 0) {
        var v = is.readUTF();
        l.addLast(v);
      }
    }
    map.entrySet().forEach(e -> e.setValue(List.copyOf(e.getValue())));
    return unmodifiableNavigableMap(map);
  }

  private LogError error() throws IOException {
    if (is.read() == 0) return null;
    var className = is.readUTF();
    var message = message();
    var cause = error();
    var suppressed = suppressed();
    var stack = stack();
    return new LogError(className, message, cause, suppressed, stack);
  }

  private List<StackTraceElement> stack() throws IOException {
    var list = new LinkedList<StackTraceElement>();
    while (is.read() != 0) {
      var classLoaderName = is.readUTF();
      var moduleName = is.readUTF();
      var moduleVersion = is.readUTF();
      var className = is.readUTF();
      var fileName = is.readUTF();
      var methodName = is.readUTF();
      var lineNumber = is.readInt();
      list.addLast(new StackTraceElement(classLoaderName, moduleName, moduleVersion, className, methodName, fileName, lineNumber));
    }
    return List.copyOf(list);
  }

  private List<LogError> suppressed() throws IOException {
    var list = new LinkedList<LogError>();
    while (true) {
      var e = error();
      if (e == null) break;
      list.addLast(e);
    }
    return List.copyOf(list);
  }

  private ThreadInfo threadInfo() throws IOException {
    var group = is.readUTF();
    var name = is.readUTF();
    var id = is.readLong();
    var priority = is.readInt();
    var state = is.read();
    var daemon = (state & 0x01) != 0;
    var interrupted = (state & 0x02) != 0;
    var virtual = (state & 0x04) != 0;
    return new ThreadInfo(group, id, name, priority, daemon, virtual, interrupted);
  }

  private static System.Logger.Level level(int level) {
    return switch (level) {
      case 0 -> System.Logger.Level.TRACE;
      case 10 -> System.Logger.Level.DEBUG;
      case 20 -> System.Logger.Level.INFO;
      case 30 -> System.Logger.Level.WARNING;
      case 40 -> System.Logger.Level.ERROR;
      default -> System.Logger.Level.OFF;
    };
  }
}
