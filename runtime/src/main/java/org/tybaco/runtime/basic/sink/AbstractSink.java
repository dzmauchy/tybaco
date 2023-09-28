package org.tybaco.runtime.basic.sink;

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
import org.tybaco.runtime.basic.Startable;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

abstract class AbstractSink implements Startable, AutoCloseable {

  final Thread thread;

  AbstractSink(ThreadFactory tf) {
    this.thread = tf.newThread(this::run);
  }

  abstract void run();

  public void daemon(boolean daemon) {
    thread.setDaemon(daemon);
  }

  public boolean daemon() {
    return thread.isDaemon();
  }

  public boolean alive() {
    return thread.isAlive();
  }

  @Override
  public void start() {
    thread.start();
  }

  void waitForState(AtomicInteger state, Consumer<InterruptedException> consumer) {
    var thread = Thread.currentThread();
    while (state.get() > 0) {
      if (thread.isInterrupted()) {
        consumer.accept(new InterruptedException());
        break;
      }
      LockSupport.parkNanos(1_000L);
    }
  }

  void processErrors(ConcurrentLinkedQueue<Throwable> exceptions, Consumer<? super Throwable> consumer) {
    exceptions.stream()
      .filter(e -> e != Break.BREAK)
      .reduce((e1, e2) -> {
        e1.addSuppressed(e2);
        return e1;
      })
      .ifPresent(consumer);
  }

  @Override
  public void close() throws Exception {
    if (thread.isAlive()) {
      if (!thread.isInterrupted()) {
        thread.interrupt();
      }
      thread.join();
    }
  }
}
