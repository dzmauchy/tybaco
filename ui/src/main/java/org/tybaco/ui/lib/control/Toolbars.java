package org.tybaco.ui.lib.control;

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

import javafx.scene.control.*;
import org.springframework.beans.factory.ObjectProvider;
import org.tybaco.ui.lib.action.Action;

import java.util.*;

public interface Toolbars {

  static void fillToolbar(ToolBar toolBar, ObjectProvider<Action> actions) {
    var groupMap = new TreeMap<String, ToggleGroup>();
    var map = new TreeMap<String, ArrayList<Action>>();
    actions.forEach(a -> map.computeIfAbsent(a.getSeparatorGroup(), k -> new ArrayList<>()).add(a));
    var items = toolBar.getItems();
    map.forEach((group, list) -> {
      list.forEach(a -> items.add(a.toSmartButton(groupMap)));
      if (map.higherKey(group) != null) {
        items.add(new Separator());
      }
    });
  }
}
