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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tybaco.runtime.basic.Startable;
import org.tybaco.runtime.basic.Break;
import org.tybaco.runtime.basic.source.Source;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public final class ParallelSink<E> implements Startable {

  private final Logger logger;
  private final Thread thread;
  private final Executor executor;
  private final Source<E> source;
  private final Consumer<? super E> consumer;
  private final Consumer<? super Throwable> onError;

  public ParallelSink(ThreadFactory tf, Executor executor, String name, Source<E> source, Consumer<? super E> consumer, Consumer<? super Throwable> onError) {
    this.thread = tf == null ? new Thread(this::run, name) : tf.newThread(this::run);
    this.logger = LoggerFactory.getLogger(name.replaceAll("\\s++", "_"));
    this.executor = executor == null ? ForkJoinPool.commonPool() : executor;
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
    var exception = new AtomicReference<Throwable>();
    try {
      source.apply(e -> {
        executor.execute(() -> {
          try {
            consumer.accept(e);
          } catch (Throwable x) {
            exception.updateAndGet(o -> merge(o, x));
          }
        });
        var x = exception.get();
        switch (x) {
          case null -> {}
          case RuntimeException ex -> throw ex;
          case Error er -> throw er;
          default -> throw new ExceptionWrapper(x);
        }
      });
    } catch (Break ignore) {
    } catch (ExceptionWrapper wrapper) {
      if (onError == null) {
        logger.error("Sink error", wrapper.getCause());
      } else {
        onError.accept(wrapper.getCause());
      }
    } catch (Throwable e) {
      if (onError == null) {
        logger.error("Sink error", e);
      } else {
        onError.accept(e);
      }
    }
  }

  @Override
  public void start() {
    thread.start();
  }

  private static Throwable merge(Throwable old, Throwable current) {
    if (old == null) {
      return current;
    } else {
      old.addSuppressed(current);
      return old;
    }
  }
}
