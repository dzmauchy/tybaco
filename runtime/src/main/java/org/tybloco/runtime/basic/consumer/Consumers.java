package org.tybloco.runtime.basic.consumer;

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

import org.tybloco.runtime.meta.*;

import java.util.concurrent.Executor;
import java.util.function.*;

@Blocks(name = "Generic consumers", icon = "嚀", description = "Generic consumers")
public interface Consumers {

  @Block(name = "A forked consumer", icon = "叉", description = "Two consumers consuming the same elements")
  static <T> Consumer<T> forked(
    @Input(name = "First consumer", icon = "友", description = "First output consumer")
    Consumer<? super T> c1,
    @Input(name = "Second consumer", icon = "友", description = "Second output consumer")
    Consumer<? super T> c2
  ) {
    return e -> {
      c1.accept(e);
      c2.accept(e);
    };
  }


  @Block(name = "A forked consumer", icon = "叉", description = "Multiple consumers consuming the same elements")
  @SafeVarargs
  static <T> Consumer<T> forkedMultiple(
    @Input(name = "Consumers", icon = "口", description = "Output consumers")
    Consumer<? super T>... consumers
  ) {
    return e -> {
      for (var c : consumers) {
        c.accept(e);
      }
    };
  }

  @Block(name = "Parallel consumer", icon = "倍", description = "Consumers elements in parallel")
  static <T> Consumer<T> parallel(
    @Input(name = "Executor", icon = "施", description = "An executor to perform tasks")
    Executor executor,
    @Input(name = "Consumer", icon = "嚀", description = "Output consumer")
    Consumer<? super T> consumer
  ) {
    return e -> executor.execute(() -> consumer.accept(e));
  }

  @Block(name = "Parallel forked consumer", icon = "枝", description = "Each consumer consumes each element with the given executor")
  static <T> Consumer<T> forkParallel(
    @Input(name = "First executor", icon = "初", description = "First executor")
    Executor e1,
    @Input(name = "First consumer", icon = "初", description = "First consumer")
    Consumer<? super T> c1,
    @Input(name = "Second executor", icon = "初", description = "First consumer")
    Executor e2,
    Consumer<? super T> c2) {
    return e -> {
      e1.execute(() -> c1.accept(e));
      e2.execute(() -> c2.accept(e));
    };
  }

  static <T> Consumer<T> toDouble(DoubleConsumer consumer, ToDoubleFunction<? super T> func) {
    return e -> consumer.accept(func.applyAsDouble(e));
  }

  static <T> Consumer<T> toLong(LongConsumer consumer, ToLongFunction<? super T> func) {
    return e -> consumer.accept(func.applyAsLong(e));
  }

  static <T> Consumer<T> toInt(IntConsumer consumer, ToIntFunction<? super T> func) {
    return e -> consumer.accept(func.applyAsInt(e));
  }
}
