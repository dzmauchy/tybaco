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

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

public interface DoubleConsumers {

  static <T> Consumer<T> clockSourceMillis(DoubleConsumer consumer) {
    return v -> consumer.accept(System.currentTimeMillis() / 1e3d);
  }

  static <T> Consumer<T> clockSourceNanos(DoubleConsumer consumer) {
    return v -> consumer.accept(System.nanoTime() / 1e9d);
  }

  static DoubleConsumer sin(DoubleConsumer consumer) {
    return v -> consumer.accept(Math.sin(v));
  }

  static DoubleConsumer cos(DoubleConsumer consumer) {
    return v -> consumer.accept(Math.cos(v));
  }

  static DoubleConsumer sinh(DoubleConsumer consumer) {
    return v -> consumer.accept(Math.sinh(v));
  }

  static DoubleConsumer cosh(DoubleConsumer consumer) {
    return v -> consumer.accept(Math.cosh(v));
  }

  static DoubleConsumer tan(DoubleConsumer consumer) {
    return v -> consumer.accept(Math.tan(v));
  }

  static DoubleConsumer tanh(DoubleConsumer consumer) {
    return v -> consumer.accept(Math.tanh(v));
  }

  static DoubleConsumer acos(DoubleConsumer consumer) {
    return v -> consumer.accept(Math.acos(v));
  }

  static DoubleConsumer asin(DoubleConsumer consumer) {
    return v -> consumer.accept(Math.asin(v));
  }

  static DoubleConsumer atan(DoubleConsumer consumer) {
    return v -> consumer.accept(Math.atan(v));
  }

  static DoubleConsumer signum(DoubleConsumer consumer) {
    return v -> consumer.accept(Math.signum(v));
  }

  static DoubleConsumer log(DoubleConsumer consumer) {
    return v -> consumer.accept(Math.log(v));
  }

  static DoubleConsumer log10(DoubleConsumer consumer) {
    return v -> consumer.accept(Math.log10(v));
  }
}
