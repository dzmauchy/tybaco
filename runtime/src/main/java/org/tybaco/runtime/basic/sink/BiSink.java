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

import org.tybaco.runtime.application.ApplicationContext;
import org.tybaco.runtime.basic.Break;
import org.tybaco.runtime.basic.executors.ExecutorByKey;
import org.tybaco.runtime.basic.source.BiSource;
import org.tybaco.runtime.meta.*;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class BiSink<K, V> extends AbstractSink {

  private final BiSource<K, V> source;
  private final ExecutorByKey<K> executors;
  private final BiConsumer<? super K, ? super V> consumer;
  private final Consumer<? super Throwable> onError;

  @Block(name = "Sink of key-value pairs", icon = "咥", description = "A sink of key-value pairs")
  public BiSink(
    @InternalInput("$applicationContext")
    ApplicationContext context,

    @InternalInput("$defaultThreadFactory")
    @Input(name = "Thread factory", icon = "縺", description = "Thread factory used to create a consumer thread")
    ThreadFactory tf,

    @Input(name = "Source", icon = "源", description = "A key-value source")
    BiSource<K, V> source,

    @InternalInput("$defaultExecutorByKey")
    @Input(name = "Executor by key", icon = "走", description = "A provider of executors by key")
    ExecutorByKey<K> exs,

    @Input(name = "Consumer", icon = "讀", description = "Key-value consumer")
    BiConsumer<? super K, ? super V> consumer,

    @InternalInput("$defaultErrorHandler")
    @Input(name = "Error handler", icon = "訥", description = "Error handler")
    Consumer<? super Throwable> onError
  ) {
    super(context, tf);
    this.source = source;
    this.executors = exs;
    this.consumer = consumer;
    this.onError = onError;
  }

  @Override
  void run() {
    var exceptions = new ConcurrentLinkedQueue<Throwable>();
    var state = new AtomicLong();
    try {
      source.apply(context, (k, v) -> {
        state.incrementAndGet();
        try {
          var executor = executors.executorByKey(k);
          executor.execute(() -> {
            try {
              consumer.accept(k, v);
            } catch (Throwable x) {
              exceptions.add(x);
            } finally {
              state.decrementAndGet();
            }
          });
        } catch (Throwable x) {
          state.decrementAndGet();
          throw x;
        }
        if (!exceptions.isEmpty()) throw Break.BREAK;
      });
    } catch (Break ignore) {
    } catch (Throwable e) {
      exceptions.add(e);
    }
    waitForState(state, exceptions::add);
    processErrors(exceptions, onError);
  }
}
