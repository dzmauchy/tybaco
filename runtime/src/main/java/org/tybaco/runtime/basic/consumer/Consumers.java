package org.tybaco.runtime.basic.consumer;

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

import java.util.concurrent.Executor;
import java.util.function.*;

public interface Consumers {

  static <T> Consumer<T> forked(Consumer<? super T> c1, Consumer<? super T> c2) {
    return e -> {
      c1.accept(e);
      c2.accept(e);
    };
  }

  @SafeVarargs
  static <T> Consumer<T> forked(Consumer<? super T>... consumers) {
    return e -> {
      for (var c : consumers) {
        c.accept(e);
      }
    };
  }

  static <T> Consumer<T> parallel(Executor executor, Consumer<? super T> consumer) {
    return e -> executor.execute(() -> consumer.accept(e));
  }

  static <T> Consumer<T> forkParallel(Executor e1, Consumer<? super T> c1, Executor e2, Consumer<? super T> c2) {
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
