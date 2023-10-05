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
import org.slf4j.helpers.AbstractLogger;

import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Pattern;

final class Logger extends AbstractLogger {

  private final ArrayBlockingQueue<LogRecord> queue;
  private final int effectiveLevel;
  private final HashMap<String, System.Logger.Level> markers;

  Logger(ArrayBlockingQueue<LogRecord> queue, String name, HashMap<Pattern, System.Logger.Level> patterns, HashMap<String, System.Logger.Level> markers) {
    this.queue = queue;
    this.name = name;
    this.effectiveLevel = patterns.entrySet().parallelStream()
      .filter(p -> p.getKey().matcher(name).matches())
      .mapToInt(e -> e.getValue().getSeverity())
      .max()
      .orElseGet(System.Logger.Level.ALL::getSeverity);
    this.markers = markers;
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
      var time = Instant.ofEpochMilli(System.currentTimeMillis());
      queue.put(new LogRecord(level, thread, time, name, marker, messagePattern, arguments, throwable));
    } catch (Throwable ignore) {
    }
  }

  @Override
  public boolean isTraceEnabled() {
    return levelEnabled(System.Logger.Level.TRACE);
  }

  @Override
  public boolean isTraceEnabled(Marker marker) {
    return markerEnabled(System.Logger.Level.TRACE, marker) && levelEnabled(System.Logger.Level.TRACE);
  }

  @Override
  public boolean isDebugEnabled() {
    return levelEnabled(System.Logger.Level.DEBUG);
  }

  @Override
  public boolean isDebugEnabled(Marker marker) {
    return markerEnabled(System.Logger.Level.DEBUG, marker) && levelEnabled(System.Logger.Level.DEBUG);
  }

  @Override
  public boolean isInfoEnabled() {
    return levelEnabled(System.Logger.Level.INFO);
  }

  @Override
  public boolean isInfoEnabled(Marker marker) {
    return markerEnabled(System.Logger.Level.INFO, marker) && levelEnabled(System.Logger.Level.INFO);
  }

  @Override
  public boolean isWarnEnabled() {
    return levelEnabled(System.Logger.Level.WARNING);
  }

  @Override
  public boolean isWarnEnabled(Marker marker) {
    return markerEnabled(System.Logger.Level.WARNING, marker) && levelEnabled(System.Logger.Level.WARNING);
  }

  @Override
  public boolean isErrorEnabled() {
    return levelEnabled(System.Logger.Level.ERROR);
  }

  @Override
  public boolean isErrorEnabled(Marker marker) {
    return markerEnabled(System.Logger.Level.ERROR, marker) && levelEnabled(System.Logger.Level.ERROR);
  }

  private boolean markerEnabled(System.Logger.Level level, Marker marker) {
    var l = markers.get(marker.getName());
    if (l != null && level.getSeverity() < l.getSeverity()) return false;
    for (var it = marker.iterator(); it.hasNext(); ) {
      if (!markerEnabled(level, it.next())) return false;
    }
    return true;
  }

  private boolean levelEnabled(System.Logger.Level level) {
    return level.getSeverity() >= effectiveLevel;
  }
}
