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

import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.tybaco.runtime.logging.LoggingServiceProvider.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.tybaco.runtime.logging.LoggingServiceProvider.initialized;

final class LoggingErrorStream extends PrintStream {

  LoggingErrorStream() {
    super(new ByteArrayOutputStream() {
      @Override
      public void flush() {
        try {
          var message = new String(buf, 0, count, StandardCharsets.UTF_8);
          buf = new byte[0];
          count = 0;
          if (initialized && LoggerFactory.getILoggerFactory() instanceof LogFactory f) {
            var queue = f.queue();
            queue.put(new LogRecord(Level.ERROR, null, message, null, null));
          }
        } catch (Throwable ignore) {
        }
      }
    }, true, StandardCharsets.UTF_8);
  }
}
