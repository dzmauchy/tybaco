package org.tybloco.util;

/*-
 * #%L
 * library
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

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public final class FastLatch extends AbstractQueuedSynchronizer {

  public FastLatch(int state) {
    setState(state);
  }

  @Override
  protected int tryAcquireShared(int acquires) {
    return (getState() == 0) ? 1 : -1;
  }

  @Override
  protected boolean tryReleaseShared(int releases) {
    for (;;) {
      int c = getState();
      if (c == 0) return false;
      int nc = c - 1;
      if (compareAndSetState(c, nc)) return nc == 0;
    }
  }

  public void await() {
    acquireShared(1);
  }

  public void countDown() {
    releaseShared(1);
  }
}
