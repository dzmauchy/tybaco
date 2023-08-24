package org.tybaco.ui.lib.utils;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public final class Latch extends AbstractQueuedSynchronizer {

  public Latch(int state) {
    setState(state);
  }

  @Override
  protected int tryAcquireShared(int acquires) {
    return getState() == 0 ? 1 : -1;
  }

  @Override
  protected boolean tryReleaseShared(int arg) {
    for (int c = getState(); c != 0; c = getState()) {
      int nc = c - 1;
      if (compareAndSetState(c, nc)) return nc == 0;
    }
    return false;
  }
}
