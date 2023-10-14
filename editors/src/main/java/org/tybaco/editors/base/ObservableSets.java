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

import javafx.application.Platform;
import javafx.collections.*;

import java.util.function.Predicate;
import java.util.function.Supplier;

public interface ObservableSets {

  static <E> ObservableSet<E> filtered(ObservableSet<E> original, Predicate<? super E> filter, Supplier<? extends ObservableSet<E>> supplier) {
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
    Platform.runLater(() -> {
      set.addAll(original);
      original.addListener(new WeakSetChangeListener<>(listener));
    });
    return set;
  }
}
