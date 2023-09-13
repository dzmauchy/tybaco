package org.tybaco.ui.lib.logging;

/*-
 * #%L
 * ui
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

import java.util.logging.*;

import static java.util.logging.Level.*;

public class Logging {

  public static final Logger LOG = LogManager.getLogManager().getLogger("");

  private Logging() {
  }

  public static LogRecord info(String message, Object... args) {
    var r = new LogRecord(INFO, message);
    if (args.length > 0) r.setParameters(args);
    return r;
  }

  public static LogRecord warn(String message, Object... args) {
    var r = new LogRecord(WARNING, message);
    if (args.length > 0) r.setParameters(args);
    return r;
  }

  public static LogRecord warn(String message, Throwable cause, Object... args) {
    var r = warn(message, args);
    r.setThrown(cause);
    return r;
  }

  public static LogRecord error(String message, Object... args) {
    var r = new LogRecord(SEVERE, message);
    if (args.length > 0) r.setParameters(args);
    return r;
  }

  public static LogRecord error(String message, Throwable cause, Object... args) {
    var r = error(message, args);
    r.setThrown(cause);
    return r;
  }
}
