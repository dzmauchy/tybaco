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

import java.util.logging.*;

public final class Log {

  private static final Logger LOGGER = LogManager.getLogManager().getLogger("");

  private Log() {
  }

  public static void log(Class<?> logger, Level level, String message) {
    var record = new LogRecord(level, message);
    record.setSourceClassName(null);
    record.setLoggerName(logger.getName());
    LOGGER.log(record);
  }

  public static void log(Class<?> logger, Level level, String message, Object... params) {
    var record = new LogRecord(level, message);
    record.setSourceClassName(null);
    record.setLoggerName(logger.getName());
    record.setParameters(params);
    LOGGER.log(record);
  }

  public static void log(Class<?> logger, Level level, String message, Throwable cause, Object... params) {
    var record = new LogRecord(level, message);
    record.setSourceClassName(null);
    record.setLoggerName(logger.getName());
    record.setParameters(params);
    record.setThrown(cause);
    LOGGER.log(record);
  }

  public static void log(String logger, Level level, String message) {
    var record = new LogRecord(level, message);
    record.setSourceClassName(null);
    record.setLoggerName(logger);
    LOGGER.log(record);
  }

  public static void log(String logger, Level level, String message, Object... params) {
    var record = new LogRecord(level, message);
    record.setSourceClassName(null);
    record.setLoggerName(logger);
    record.setParameters(params);
    LOGGER.log(record);
  }

  public static void log(String logger, Level level, String message, Throwable cause, Object... params) {
    var record = new LogRecord(level, message);
    record.setSourceClassName(null);
    record.setLoggerName(logger);
    record.setParameters(params);
    record.setThrown(cause);
    LOGGER.log(record);
  }

  public static void info(Class<?> logger, String message) {
    var record = new LogRecord(Level.INFO, message);
    record.setSourceClassName(null);
    record.setLoggerName(logger.getName());
    LOGGER.log(record);
  }

  public static void info(Class<?> logger, String message, Object... params) {
    var record = new LogRecord(Level.INFO, message);
    record.setSourceClassName(null);
    record.setLoggerName(logger.getName());
    record.setParameters(params);
    LOGGER.log(record);
  }

  public static void debug(Class<?> logger, String message) {
    var record = new LogRecord(Level.FINE, message);
    record.setSourceClassName(null);
    record.setLoggerName(logger.getName());
    LOGGER.log(record);
  }

  public static void debug(Class<?> logger, String message, Object... params) {
    var record = new LogRecord(Level.FINE, message);
    record.setSourceClassName(null);
    record.setLoggerName(logger.getName());
    record.setParameters(params);
    LOGGER.log(record);
  }

  public static void debug(Class<?> logger, String message, Throwable cause, Object... params) {
    var record = new LogRecord(Level.FINE, message);
    record.setSourceClassName(null);
    record.setLoggerName(logger.getName());
    record.setParameters(params);
    record.setThrown(cause);
    LOGGER.log(record);
  }

  public static void warn(Class<?> logger, String message) {
    var record = new LogRecord(Level.WARNING, message);
    record.setSourceClassName(null);
    record.setLoggerName(logger.getName());
    LOGGER.log(record);
  }

  public static void warn(Class<?> logger, String message, Object... params) {
    var record = new LogRecord(Level.WARNING, message);
    record.setSourceClassName(null);
    record.setLoggerName(logger.getName());
    record.setParameters(params);
    LOGGER.log(record);
  }

  public static void warn(Class<?> logger, String message, Throwable cause, Object... params) {
    var record = new LogRecord(Level.WARNING, message);
    record.setSourceClassName(null);
    record.setLoggerName(logger.getName());
    record.setParameters(params);
    record.setThrown(cause);
    LOGGER.log(record);
  }

  public static void error(Class<?> logger, String message) {
    var record = new LogRecord(Level.SEVERE, message);
    record.setSourceClassName(null);
    record.setLoggerName(logger.getName());
    LOGGER.log(record);
  }

  public static void error(Class<?> logger, String message, Object... params) {
    var record = new LogRecord(Level.SEVERE, message);
    record.setSourceClassName(null);
    record.setLoggerName(logger.getName());
    record.setParameters(params);
    LOGGER.log(record);
  }

  public static void error(Class<?> logger, String message, Throwable cause, Object... params) {
    var record = new LogRecord(Level.SEVERE, message);
    record.setSourceClassName(null);
    record.setLoggerName(logger.getName());
    record.setParameters(params);
    record.setThrown(cause);
    LOGGER.log(record);
  }
}
