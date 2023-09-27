package org.tybaco.runtime.basic.sink;

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

import org.tybaco.runtime.annotations.Sink;
import org.tybaco.runtime.basic.Break;
import org.tybaco.runtime.basic.Startable;
import org.tybaco.runtime.basic.source.Source;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

@Sink
public final class ParallelSink<E> implements Startable {

  private final Thread thread;
  private final Executor executor;
  private final Source<E> source;
  private final Consumer<? super E> consumer;
  private final Consumer<? super Throwable> onError;

  public ParallelSink(ThreadFactory tf, Executor executor, Source<E> source, Consumer<? super E> consumer, Consumer<? super Throwable> onError) {
    this.thread = tf.newThread(this::run);
    this.executor = executor;
    this.source = source;
    this.consumer = consumer;
    this.onError = onError;
  }

  public void daemon(boolean daemon) {
    thread.setDaemon(daemon);
  }

  public boolean daemon() {
    return thread.isDaemon();
  }

  public boolean alive() {
    return thread.isAlive();
  }

  private void run() {
    var exceptions = new ConcurrentLinkedQueue<Throwable>();
    var state = new AtomicInteger();
    try {
      source.apply(e -> {
        state.incrementAndGet();
        executor.execute(() -> {
          try {
            consumer.accept(e);
          } catch (Throwable x) {
            exceptions.add(x);
          } finally {
            state.decrementAndGet();
          }
        });
        if (!exceptions.isEmpty()) throw Break.BREAK;
      });
    } catch (Break ignore) {
    } catch (Throwable e) {
      exceptions.add(e);
    }
    var thread = Thread.currentThread();
    while (state.get() > 0) {
      if (thread.isInterrupted()) {
        exceptions.add(new InterruptedException());
        break;
      }
      LockSupport.parkNanos(1_000L);
    }
    exceptions.stream()
      .filter(e -> e != Break.BREAK)
      .reduce((e1, e2) -> {
        e1.addSuppressed(e2);
        return e1;
      })
      .ifPresent(onError);
  }

  @Override
  public void start() {
    thread.start();
  }
}
