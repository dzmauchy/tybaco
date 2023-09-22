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
import java.util.function.LongConsumer;

public final class SyncLongSource implements LongSource {

  private final LongSource source;
  private final long periodNanos;

  public SyncLongSource(LongSource source, long periodNanos) {
    this.source = source;
    this.periodNanos = periodNanos;
  }

  @Override
  public void apply(LongConsumer consumer) {
    source.apply(new SyncConsumer(consumer, periodNanos));
  }

  private static final class SyncConsumer implements LongConsumer {

    private final LongConsumer consumer;
    private final long periodNanos;
    private long lastTime;

    private SyncConsumer(LongConsumer consumer, long periodNanos) {
      this.consumer = consumer;
      this.periodNanos = periodNanos;
      this.lastTime = System.nanoTime() - periodNanos;
    }

    @Override
    public void accept(long o) {
      var time = System.nanoTime() - lastTime;
      if (time < periodNanos) {
        LockSupport.parkNanos(periodNanos - time);
      }
      lastTime = System.nanoTime();
      consumer.accept(o);
    }
  }
}
