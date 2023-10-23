package org.tybloco.editors.base;

/*-
 * #%L
 * editors
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

import javafx.collections.*;

import java.util.Objects;
import java.util.function.Function;

public interface ObservableSets {

  static <E, R> Runnable synchronizeSet(ObservableSet<E> original, ObservableList<R> list, Function<? super E, ? extends R> func, Function<? super R, ? extends E> reversed) {
    var changeListener = (SetChangeListener<E>) c -> {
      if (c.wasRemoved()) {
        list.removeIf(v -> Objects.equals(reversed.apply(v), c.getElementRemoved()));
      } else if (c.wasAdded()) {
        list.add(func.apply(c.getElementAdded()));
      }
    };
    original.addListener(new WeakSetChangeListener<>(changeListener));
    var reset = (Runnable) () -> {
      Objects.requireNonNull(changeListener);
      list.setAll(original.stream().map(func).toList());
    };
    reset.run();
    return reset;
  }
}
