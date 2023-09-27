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

import org.tybaco.runtime.annotations.Sink;
import org.tybaco.runtime.basic.Break;
import org.tybaco.runtime.basic.Startable;
import org.tybaco.runtime.basic.source.Source;

import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

@Sink
public final class SequentialSink<E> implements Startable, AutoCloseable {

  private final Thread thread;
  private final Source<E> source;
  private final Consumer<? super E> consumer;
  private final Consumer<? super Throwable> onError;

  public SequentialSink(ThreadFactory tf, Source<E> source, Consumer<? super E> consumer, Consumer<? super Throwable> onError) {
    this.thread = tf.newThread(this::run);
    this.source = source;
    this.consumer = consumer;
    this.onError = onError;
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

  private void run() {
    try {
      source.apply(consumer);
    } catch (Break ignore) {
    } catch (Throwable e) {
      onError.accept(e);
    }
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
