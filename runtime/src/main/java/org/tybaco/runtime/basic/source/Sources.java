package org.tybaco.runtime.basic.source;

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

import org.tybaco.runtime.basic.Break;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.*;

import static java.util.concurrent.locks.LockSupport.parkNanos;

public interface Sources {

  static <I, O> Source<O> transform(Source<I> source, Function<I, O> transform) {
    return consumer -> source.apply(transform::apply);
  }

  static <E> DoubleSource transformToDouble(Source<E> source, ToDoubleFunction<E> transform) {
    return consumer -> source.apply(transform::applyAsDouble);
  }

  static <E> IntSource transformToInt(Source<E> source, ToIntFunction<E> transform) {
    return consumer -> source.apply(transform::applyAsInt);
  }

  static <E> LongSource transformToLong(Source<E> source, ToLongFunction<E> transform) {
    return consumer -> source.apply(transform::applyAsLong);
  }

  static <E> Source<E> transformFromDouble(DoubleSource source, DoubleFunction<E> transform) {
    return consumer -> source.apply(transform::apply);
  }

  static <E> Source<E> transformFromInt(IntSource source, IntFunction<E> transform) {
    return consumer -> source.apply(transform::apply);
  }

  static <E> Source<E> transformFromLong(LongSource source, LongFunction<E> transform) {
    return consumer -> source.apply(transform::apply);
  }

  static <E> Source<E> untilSource(Source<E> source, Predicate<E> predicate) {
    return consumer -> source.apply(e -> {
      if (predicate.test(e)) throw Break.BREAK;
      else consumer.accept(e);
    });
  }

  static <E> Source<E> whileSource(Source<E> source, Predicate<E> predicate) {
    return consumer -> source.apply(e -> {
      if (predicate.test(e)) consumer.accept(e);
      else throw Break.BREAK;
    });
  }

  static DoubleSource untilDoubleSource(DoubleSource source, DoublePredicate predicate) {
    return consumer -> source.apply(e -> {
      if (predicate.test(e)) throw Break.BREAK;
      else consumer.accept(e);
    });
  }

  static DoubleSource whileDoubleSource(DoubleSource source, DoublePredicate predicate) {
    return consumer -> source.apply(e -> {
      if (predicate.test(e)) consumer.accept(e);
      else throw Break.BREAK;
    });
  }

  static IntSource untilIntSource(IntSource source, IntPredicate predicate) {
    return consumer -> source.apply(e -> {
      if (predicate.test(e)) throw Break.BREAK;
      else consumer.accept(e);
    });
  }

  static IntSource whileIntSource(IntSource source, IntPredicate predicate) {
    return consumer -> source.apply(e -> {
      if (predicate.test(e)) consumer.accept(e);
      else throw Break.BREAK;
    });
  }

  static LongSource untilLongSource(LongSource source, LongPredicate predicate) {
    return consumer -> source.apply(e -> {
      if (predicate.test(e)) throw Break.BREAK;
      else consumer.accept(e);
    });
  }

  static LongSource whileLongSource(LongSource source, LongPredicate predicate) {
    return consumer -> source.apply(e -> {
      if (predicate.test(e)) consumer.accept(e);
      else throw Break.BREAK;
    });
  }

  static <E> Source<E> syncSource(Source<E> source, Duration period) {
    var nanos = period.toNanos();
    var lastTime = new AtomicLong(System.nanoTime() - nanos);
    return consumer -> source.apply(e -> waitIfNecessary(lastTime, nanos, () -> consumer.accept(e)));
  }

  static DoubleSource syncDoubleSource(DoubleSource source, Duration period) {
    var nanos = period.toNanos();
    var lastTime = new AtomicLong(System.nanoTime() - nanos);
    return consumer -> source.apply(e -> waitIfNecessary(lastTime, nanos, () -> consumer.accept(e)));
  }

  static IntSource syncIntSource(IntSource source, Duration period) {
    var nanos = period.toNanos();
    var lastTime = new AtomicLong(System.nanoTime() - nanos);
    return consumer -> source.apply(e -> waitIfNecessary(lastTime, nanos, () -> consumer.accept(e)));
  }

  static LongSource syncLongSource(LongSource source, Duration period) {
    var nanos = period.toNanos();
    var lastTime = new AtomicLong(System.nanoTime() - nanos);
    return consumer -> source.apply(e -> waitIfNecessary(lastTime, nanos, () -> consumer.accept(e)));
  }

  private static void waitIfNecessary(AtomicLong lastTime, long period, Runnable task) {
    var time = System.nanoTime() - lastTime.get();
    if (time < period) parkNanos(period - time);
    lastTime.set(System.nanoTime());
    task.run();
  }
}
