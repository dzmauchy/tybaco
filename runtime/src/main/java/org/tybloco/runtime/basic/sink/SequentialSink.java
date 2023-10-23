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
import org.tybloco.runtime.basic.source.Source;
import org.tybloco.runtime.meta.*;

import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

public final class SequentialSink<E> extends AbstractSink {

  private final Source<E> source;
  private final Consumer<? super E> consumer;
  private final Consumer<? super Throwable> onError;

  @Block(name = "Sequential sink of values", icon = "的", description = "Sequential sink of values")
  public SequentialSink(
    @InternalInput("$applicationContext")
    ApplicationContext context,

    @InternalInput("$defaultThreadFactory")
    @Input(name = "Thread factory", icon = "縺", description = "Thread factory used to create a consumer thread")
    ThreadFactory tf,

    @Input(name = "Source", icon = "源", description = "A value source")
    Source<E> source,

    @Input(name = "Consumer", icon = "讀", description = "Value consumer")
    Consumer<? super E> consumer,

    @InternalInput("$defaultErrorHandler")
    @Input(name = "Error handler", icon = "訥", description = "Error handler")
    Consumer<? super Throwable> onError
  ) {
    super(context, tf);
    this.source = source;
    this.consumer = consumer;
    this.onError = onError;
  }

  @Override
  void run() {
    try {
      source.apply(context, consumer);
    } catch (Break ignore) {
    } catch (Throwable e) {
      onError.accept(e);
    }
  }
}
