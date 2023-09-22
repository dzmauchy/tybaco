package org.tybaco.logging;

/*-
 * #%L
 * library
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.logging.*;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoField.*;
import static java.util.Locale.UK;

public class FastConsoleHandler extends Handler {

  private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
    .appendValue(YEAR, 4).appendLiteral('-').appendValue(MONTH_OF_YEAR, 2).appendLiteral('-').appendValue(DAY_OF_MONTH, 2)
    .appendLiteral('T')
    .appendValue(HOUR_OF_DAY, 2).appendLiteral(':').appendValue(MINUTE_OF_HOUR, 2).appendLiteral(':').appendValue(SECOND_OF_MINUTE, 2)
    .appendLiteral('.')
    .appendValue(MILLI_OF_SECOND, 3)
    .toFormatter(UK);

  @Override
  public void publish(LogRecord record) {
    var writer = new StringWriter(128);
    var buffer = writer.getBuffer();
    DATE_TIME_FORMATTER.formatTo(record.getInstant().atZone(UTC), buffer);
    buffer
      .append(' ').append(level(record.getLevel()))
      .append(" [").append(record.getLongThreadID()).append("] ")
      .append(loggerName(record)).append(' ');
    var params = record.getParameters();
    if (params == null || params.length == 0) {
      buffer.append(record.getMessage());
    } else {
      try {
        var fmt = new MessageFormat(record.getMessage());
        fmt.format(params, buffer, null);
      } catch (Exception e) {
        buffer.append(record.getMessage());
      }
    }
    var thrown = record.getThrown();
    if (thrown != null) {
      buffer.append(System.lineSeparator());
      try (var w = new PrintWriter(writer)) {
        thrown.printStackTrace(w);
      }
    }
    System.out.println(buffer);
  }

  @Override
  public void flush() {
    System.out.flush();
  }

  @Override
  public void close() {
  }

  private static char level(Level level) {
    return switch (level.intValue()) {
      case 1000 -> 'E';
      case 900 -> 'W';
      case 800 -> 'I';
      case 700 -> 'C';
      case 500 -> 'D';
      case 400 -> 'T';
      case 300 -> 'F';
      default -> 'U';
    };
  }

  private static String loggerName(LogRecord record) {
    var loggerName = record.getLoggerName();
    if (loggerName == null) {
      return "";
    }
    var idx = loggerName.lastIndexOf('.');
    if (idx >= 0) {
      return loggerName.substring(idx + 1);
    }
    return loggerName;
  }
}
