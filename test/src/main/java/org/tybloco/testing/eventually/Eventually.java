package org.tybloco.testing.eventually;

/*-
 * #%L
 * test
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

import com.google.common.util.concurrent.UncheckedTimeoutException;

import java.time.Duration;
import java.util.concurrent.Callable;

import static java.lang.System.nanoTime;
import static java.util.concurrent.locks.LockSupport.parkNanos;

public interface Eventually {

  default <R> R eventually(Duration period, Duration delay, Callable<R> callable) {
    var start = nanoTime();
    var periodNanos = period.toNanos();
    var sleepNanos = delay.toNanos();
    var error = (Throwable) null;
    while (nanoTime() - start < periodNanos) {
      try {
        return callable.call();
      } catch (Throwable e) {
        error = e;
        parkNanos(sleepNanos);
      }
    }
    if (error == null) throw new UncheckedTimeoutException();
    else {
      switch (error) {
        case RuntimeException e -> throw e;
        case Error e -> throw e;
        case Throwable e -> throw new IllegalStateException(e);
      }
    }
  }

  default <R> R eventually(Duration period, Callable<R> callable) {
    return eventually(period, Duration.ofNanos(1L), callable);
  }

  default <R> R eventually(Callable<R> callable) {
    return eventually(Duration.ofMinutes(1L), callable);
  }
}
