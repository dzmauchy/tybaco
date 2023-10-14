package org.tybaco.editors.base;

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

import javafx.beans.Observable;
import javafx.collections.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.function.*;

public interface ObservableSets {

  static <E> ObservableSet<E> filteredSet(ObservableSet<E> original, Predicate<? super E> filter, Supplier<? extends ObservableSet<E>> supplier) {
    var set = supplier.get();
    var listener = (SetChangeListener<E>) c -> {
      if (c.getSet() != set) {
        if (c.wasRemoved()) {
          if (filter.test(c.getElementRemoved())) {
            set.remove(c.getElementRemoved());
          }
        } else if (c.wasAdded()) {
          if (filter.test(c.getElementAdded())) {
            set.add(c.getElementAdded());
          }
        }
      }
    };
    set.addListener(listener);
    set.addAll(original);
    original.addListener(new WeakSetChangeListener<>(listener));
    return set;
  }

  static <E> ObservableSet<E> filteredSet(ObservableSet<E> original, Predicate<? super E> filter) {
    return filteredSet(original, filter, () -> FXCollections.observableSet(new HashSet<>()));
  }

  static <E, R> void synchronizeSet(ObservableSet<E> original, ObservableList<R> list, Function<? super E, ? extends R> func, Function<? super R, ? extends E> reversed) {
    var changeListener = (SetChangeListener<E>) c -> {
      if (c == null) return;
      if (c.wasRemoved()) {
        list.removeIf(v -> Objects.equals(reversed.apply(v), c.getElementRemoved()));
      } else if (c.wasAdded()) {
        list.add(func.apply(c.getElementAdded()));
      }
    };
    list.addListener((Observable o) -> changeListener.onChanged(null));
    original.addListener(new WeakSetChangeListener<>(changeListener));
    list.addAll(original.stream().map(func).toList());
  }
}
