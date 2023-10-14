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

import javafx.collections.*;

import java.util.Objects;
import java.util.function.Function;

public interface ObservableLists {

  static <E, R> Runnable synchronizeLists(ObservableList<E> original, ObservableList<R> result, Function<? super E, ? extends R> func) {
    var changeListener = (ListChangeListener<E>) c -> {
      while (c.next()) {
        if (c.wasRemoved()) {
          result.remove(c.getFrom(), c.getTo());
        }
        if (c.wasAdded()) {
          result.addAll(c.getFrom(), c.getAddedSubList().stream().map(func).toList());
        }
      }
    };
    var reset = (Runnable) () -> {
      Objects.requireNonNull(changeListener);
      result.setAll(original.stream().map(func).toList());
    };
    reset.run();
    original.addListener(new WeakListChangeListener<>(changeListener));
    return reset;
  }
}
