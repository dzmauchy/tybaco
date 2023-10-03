package org.tybaco.runtime.basic.executors;

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

import java.util.LinkedList;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.SECONDS;

public final class VirtualExecutorByKey<K> implements ExecutorByKey<K>, AutoCloseable {

  private final String name;
  private final ConcurrentHashMap<K, ThreadPoolExecutor> executors;
  private volatile boolean closed;

  public VirtualExecutorByKey(String name, int expectedSize) {
    this.name = name;
    this.executors = new ConcurrentHashMap<>(expectedSize, 0.5f);
  }

  @Override
  public ThreadPoolExecutor executorByKey(K key) {
    return executors.computeIfAbsent(key, this::newExecutor);
  }

  private ThreadFactory threadFactory(K key) {
    return r -> Thread.ofVirtual().name(name + "_" + key).unstarted(r);
  }

  private ThreadPoolExecutor newExecutor(K key) {
    if (closed) throw new RejectedExecutionException(name + " is closed and cannot invoke new tasks");
    var queue = new SynchronousQueue<Runnable>();
    return new ThreadPoolExecutor(1, 1, 0L, SECONDS, queue, threadFactory(key), (r, e) -> {
      var thread = Thread.currentThread();
      while (!e.isShutdown()) {
        try {
          if (queue.offer(r, 1L, SECONDS)) break;
        } catch (InterruptedException ignore) {
          thread.interrupt();
          break;
        }
      }
    });
  }

  @Override
  public void close() {
    closed = true;
    var exceptions = new LinkedList<Throwable>();
    executors.forEach((k, v) -> {
      try {
        v.shutdown();
      } catch (Throwable e) {
        try {
          v.shutdownNow();
        } catch (Throwable x) {
          e.addSuppressed(x);
        }
        exceptions.addLast(new IllegalStateException("Unable to close " + key(k), e));
      }
    });
    executors.entrySet().removeIf(e -> {
      var k = e.getKey();
      var v = e.getValue();
      try {
        v.close();
      } catch (Throwable x) {
        exceptions.addLast(new IllegalStateException("Unable to close " + key(k), x));
      }
      return true;
    });
    if (!exceptions.isEmpty()) {
      var x = new IllegalStateException("Unable to close " + name);
      for (var e : exceptions) {
        x.addSuppressed(e);
      }
      throw x;
    }
  }

  private static String key(Object k) {
    try {
      return String.valueOf(k);
    } catch (Throwable e) {
      return k.getClass().getName() + "[" + e + "]";
    }
  }
}
