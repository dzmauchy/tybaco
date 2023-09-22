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

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import static org.assertj.core.api.Assertions.assertThat;

class SyncSourceTest {

  @Test
  void syncSourceWithoutLoad() {
    var warmupCount = 10;
    var count = 10;
    var source = (Source<String>) c -> {
      for (int i = 0; i < warmupCount; i++) {
        c.accept("warmup");
      }
      for (int i = 0; i < count; i++) {
        c.accept(null);
      }
    };
    var times = new long[count];
    var counter = new AtomicInteger();
    var syncSource = new SyncSource<>(source, 10_000_000L);
    syncSource.apply(v -> {
      if (v == null) {
        times[counter.getAndIncrement()] = System.nanoTime();
      }
    });
    for (int i = 1; i < times.length; i++) {
      var dt = times[i] - times[i - 1];
      assertThat(dt).isGreaterThanOrEqualTo(10_000_000L);
      assertThat(dt).isLessThan(11_000_000L);
    }
  }

  @Test
  void syncSourceWithLoad() {
    var warmupCount = 10;
    var count = 10;
    var source = (Source<String>) c -> {
      for (int i = 0; i < warmupCount; i++) {
        c.accept("warmup");
      }
      for (int i = 0; i < count; i++) {
        c.accept(null);
      }
    };
    var times = new long[count];
    var counter = new AtomicInteger();
    var syncSource = new SyncSource<>(source, 10_000_000L);
    syncSource.apply(v -> {
      if (v == null) {
        times[counter.getAndIncrement()] = System.nanoTime();
        LockSupport.parkNanos(5_000_000L);
      }
    });
    for (int i = 1; i < times.length; i++) {
      var dt = times[i] - times[i - 1];
      assertThat(dt).isGreaterThanOrEqualTo(10_000_000L);
      assertThat(dt).isLessThan(11_000_000L);
    }
  }
}
