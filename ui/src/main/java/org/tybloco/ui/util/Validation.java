package org.tybloco.ui.util;

/*-
 * #%L
 * ui
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

import java.util.function.*;

public sealed interface Validation<T> {

  T get(Supplier<? extends T> supplier);
  T get(Function<String, ? extends T> function);
  <R> Validation<R> map(Function<? super T, ? extends R> function);
  <R> Validation<R> flatMap(Function<? super T, ? extends Validation<R>> function);
  Validation<? extends T> or(Supplier<? extends Validation<? extends T>> supplier);
  void forEach(Consumer<? super T> consumer);

  record OK<T>(T result) implements Validation<T> {

    @Override
    public T get(Supplier<? extends T> supplier) {
      return result;
    }

    @Override
    public T get(Function<String, ? extends T> function) {
      return result;
    }

    @Override
    public <R> Validation<R> map(Function<? super T, ? extends R> function) {
      return new OK<>(function.apply(result));
    }

    @Override
    public <R> Validation<R> flatMap(Function<? super T, ? extends Validation<R>> function) {
      return function.apply(result);
    }

    @Override
    public Validation<T> or(Supplier<? extends Validation<? extends T>> supplier) {
      return this;
    }

    @Override
    public void forEach(Consumer<? super T> consumer) {
      consumer.accept(result);
    }
  }

  record Failure<T>(String message) implements Validation<T> {

    @Override
    public T get(Supplier<? extends T> supplier) {
      return supplier.get();
    }

    @Override
    public T get(Function<String, ? extends T> function) {
      return function.apply(message);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> Validation<R> map(Function<? super T, ? extends R> function) {
      return (Failure<R>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> Validation<R> flatMap(Function<? super T, ? extends Validation<R>> function) {
      return (Failure<R>) this;
    }

    @Override
    public Validation<? extends T> or(Supplier<? extends Validation<? extends T>> supplier) {
      return supplier.get();
    }

    @Override
    public void forEach(Consumer<? super T> consumer) {
    }
  }
}
