package org.tybloco.runtime.logging;

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

final class Logger extends AbstractLogger {

  private final int effectiveLevel;
  private final LoggingServiceProvider provider;

  Logger(LoggingServiceProvider provider, String name) {
    this.provider = provider;
    this.name = name;
    this.effectiveLevel = provider.patternFilters.entrySet().parallelStream()
      .filter(p -> p.getKey().matcher(name).matches())
      .mapToInt(e -> e.getValue().getSeverity())
      .max()
      .orElseGet(System.Logger.Level.ALL::getSeverity);
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
    var thread = Thread.currentThread();
    var time = Instant.ofEpochMilli(System.currentTimeMillis());
    provider.put(new LogRecord(level, thread, time, name, marker, messagePattern, arguments, throwable, provider.getMDCAdapter()));
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
    var l = provider.markerFilters.get(marker.getName());
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
