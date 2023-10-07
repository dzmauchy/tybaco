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
}
