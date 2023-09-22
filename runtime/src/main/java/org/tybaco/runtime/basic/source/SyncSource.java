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

import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

public record SyncSource<E>(Source<E> source, long periodNanos) implements Source<E> {

  @Override
  public void apply(Consumer<? super E> consumer) {
    source.apply(new SyncConsumer<>(consumer, periodNanos));
  }

  private static final class SyncConsumer<E> implements Consumer<E> {

    private final Consumer<? super E> consumer;
    private final long periodNanos;
    private long lastTime;

    private SyncConsumer(Consumer<? super E> consumer, long periodNanos) {
      this.consumer = consumer;
      this.periodNanos = periodNanos;
      this.lastTime = System.nanoTime() - periodNanos;
    }

    @Override
    public void accept(E o) {
      var time = System.nanoTime() - lastTime;
      if (time < periodNanos) {
        LockSupport.parkNanos(periodNanos - time);
      }
      lastTime = System.nanoTime();
      consumer.accept(o);
    }
  }
}
