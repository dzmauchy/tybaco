package org.tybaco.ui.lib.menus;

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

import org.springframework.beans.factory.ObjectProvider;
import org.tybaco.ui.lib.actions.SmartAction;

import javax.swing.*;

public final class Menus {

  private Menus() {
  }

  public static void addMenuItems(JMenu menu, ObjectProvider<SmartAction> actions) {
    var grouped = SmartAction.group(actions);
    for (var it = grouped.values().iterator(); it.hasNext(); ) {
      var l = it.next();
      l.forEach(menu::add);
      if (it.hasNext()) menu.addSeparator();
    }
  }

  public static void addMenuItems(JPopupMenu menu, ObjectProvider<SmartAction> map) {
    var grouped = SmartAction.group(map);
    for (var it = grouped.values().iterator(); it.hasNext(); ) {
      var l = it.next();
      l.forEach(menu::add);
      if (it.hasNext()) menu.addSeparator();
    }
  }
}
