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

import java.time.Instant;

record LogRecord(Level level, Thread thread, Instant time, String logger, Marker marker, String msg, Object[] args, Throwable throwable) {

  void writeTo(FileBuffer buffer) {
    buffer.write('{');
    buffer.writeKey("@timestamp");
    buffer.write(':');
    buffer.writeQuotedString(time.toString());
    buffer.write('}');
  }
}
