package org.tybaco.runtime.basic.thread;

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

import org.jetbrains.annotations.NotNull;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class VirtualThreadFactory implements ThreadFactory {

  private final AtomicInteger counter = new AtomicInteger();
  private final String pattern;
  private final UncaughtExceptionHandler exceptionHandler;

  public VirtualThreadFactory(String pattern, UncaughtExceptionHandler exceptionHandler) {
    this.pattern = pattern;
    this.exceptionHandler = exceptionHandler;
  }

  @Override
  public Thread newThread(@NotNull Runnable r) {
    return Thread.ofVirtual()
      .name(pattern.formatted(counter.getAndIncrement()))
      .uncaughtExceptionHandler(exceptionHandler)
      .unstarted(r);
  }

  @Override
  public String toString() {
    return pattern;
  }
}
