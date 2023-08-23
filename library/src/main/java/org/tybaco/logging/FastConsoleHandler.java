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
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static java.time.ZoneOffset.UTC;

public class FastConsoleHandler extends Handler {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_DATE)
            .appendLiteral('T')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .appendLiteral('.')
            .appendValue(ChronoField.MILLI_OF_SECOND, 3)
            .toFormatter(Locale.UK);

    @Override
    public void publish(LogRecord record) {
        var writer = new StringWriter(128);
        var buffer = writer.getBuffer();
        DATE_TIME_FORMATTER.formatTo(record.getInstant().atZone(UTC), buffer);
        buffer
                .append(' ').append((record.getLevel().intValue() / 100) - 1)
                .append(" [").append(record.getLongThreadID()).append("] ");
        var method = record.getSourceMethodName();
        var className = record.getSourceClassName();
        if (method != null && className != null) {
            buffer.append(className).append('.').append(method).append(' ');
        } else {
            buffer.append(record.getLoggerName()).append(' ');
        }
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
}
