package org.tybaco.runtime.application;

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

import org.tybaco.runtime.exception.ApplicationCloseException;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class ApplicationContext {

  public final Thread mainThread;
  private final AtomicReference<ApplicationCloseables> closeables = new AtomicReference<>();
  private final ConcurrentHashMap<String, Object> blocks = new ConcurrentHashMap<>(1024, 0.75f);
  private final ConcurrentHashMap<String, ConcurrentHashMap<String, Object>> outputs = new ConcurrentHashMap<>(1024, 0.75f);
  private volatile boolean running = true;

  public ApplicationContext(Thread thread) {
    mainThread = thread;
  }

  public ApplicationContext() {
    this(Thread.currentThread());
  }

  public boolean isRunning() {
    return running;
  }

  @SuppressWarnings("unchecked")
  public <T> T block(String block) {
    var b = blocks.get(block);
    if (b == null) throw new NoSuchElementException(block);
    return (T) b;
  }

  @SuppressWarnings("unchecked")
  public <T> T block(String block, Supplier<T> supplier) {
    var b = (T) blocks.computeIfAbsent(block, k -> supplier.get());
    if (b instanceof AutoCloseable c) {
      closeables.updateAndGet(o -> new ApplicationCloseables(c, o));
    }
    return b;
  }

  @SuppressWarnings("unchecked")
  public <T> T output(String block, String out, Supplier<T> supplier) {
    return (T) outputs
      .computeIfAbsent(block, k -> new ConcurrentHashMap<>(8, 0.75f))
      .computeIfAbsent(out, k -> supplier.get());
  }

  public void close() {
    running = false;
    var closeException = new ApplicationCloseException();
    for (var c = closeables.get(); c != null; c = c.previous()) {
      try {
        c.closeable().close();
      } catch (Throwable e) {
        closeException.addSuppressed(e);
      }
    }
    if (closeException.getSuppressed().length > 0) throw closeException;
  }
}
