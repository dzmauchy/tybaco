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

import com.google.common.collect.Lists;
import javafx.application.Platform;
import javafx.collections.*;
import org.tybaco.util.MiscOps;

import java.util.function.Function;

public interface ObservableLists {
  
  static <E, R> void synchronizeLists(ObservableList<E> original, ObservableList<R> result, Function<? super E, ? extends R> func) {
    var changeListener = (ListChangeListener<E>) c -> {
      if (c.getList() == result) return;
      while (c.next()) {
        if (c.wasRemoved()) {
          result.remove(c.getFrom(), c.getTo());
        }
        if (c.wasAdded()) {
          result.addAll(c.getFrom(), Lists.transform(c.getAddedSubList(), func::apply));
        }
      }
    };
    result.addListener(MiscOps.<ListChangeListener<R>>cast(changeListener));
    Platform.runLater(() -> {
      result.setAll(original.stream().map(func).toList());
      original.addListener(new WeakListChangeListener<>(changeListener));
    });
  }
}
