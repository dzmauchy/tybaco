package org.tybloco.runtime.basic.sink;

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

import org.tybloco.runtime.application.ApplicationContext;
import org.tybloco.runtime.basic.Break;
import org.tybloco.runtime.basic.Startable;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static java.util.concurrent.locks.LockSupport.parkNanos;

abstract class AbstractSink implements Startable, AutoCloseable {

  final ApplicationContext context;
  final Thread thread;

  AbstractSink(ApplicationContext context, ThreadFactory tf) {
    this.context = context;
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

  void waitForState(AtomicLong state, Consumer<InterruptedException> consumer) {
    var thread = Thread.currentThread();
    while (state.get() > 0L) {
      if (thread.isInterrupted()) {
        consumer.accept(new InterruptedException());
        break;
      }
      parkNanos(1_000L);
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
