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
import org.tybaco.runtime.basic.source.BiSource;

import java.util.concurrent.ThreadFactory;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class SequentialBiSink<K, V> extends AbstractSink {

  private final BiSource<K, V> source;
  private final BiConsumer<? super K, ? super V> consumer;
  private final Consumer<? super Throwable> onError;

  public SequentialBiSink(ThreadFactory tf, BiSource<K, V> source, BiConsumer<? super K, ? super V> consumer, Consumer<? super Throwable> onError) {
    super(tf);
    this.source = source;
    this.consumer = consumer;
    this.onError = onError;
  }

  @Override
  void run() {
    try {
      source.apply(consumer);
    } catch (Break ignore) {
    } catch (Throwable e) {
      onError.accept(e);
    }
  }
}
