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

import org.tybaco.runtime.basic.Starteable;

import java.util.function.Consumer;

import static org.tybaco.runtime.basic.source.Break.BREAK;

public final class SequentialSink<E> implements Starteable, AutoCloseable {

  private final Thread thread;
  private final Source<E> source;
  private final Consumer<? super E> consumer;

  public SequentialSink(ThreadGroup threadGroup, String name, Source<E> source, Consumer<? super E> consumer) {
    this.thread = new Thread(threadGroup, this::run, name);
    this.source = source;
    this.consumer = consumer;
  }

  public void daemon(boolean daemon) {
    thread.setDaemon(daemon);
  }

  public boolean daemon() {
    return thread.isDaemon();
  }

  public boolean alive() {
    return thread.isAlive();
  }

  private void consume(E element) {
    consumer.accept(element);
    if (thread.isInterrupted()) {
      throw BREAK;
    }
  }

  private void run() {
    source.apply(this::consume);
  }

  @Override
  public void start() {
    thread.start();
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
