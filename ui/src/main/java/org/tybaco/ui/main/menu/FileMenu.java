package org.tybaco.ui.main.menu;

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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.tybaco.ui.lib.actions.SmartAction;
import org.tybaco.ui.lib.menus.Menus;

import javax.swing.*;
import java.util.Map;

@Component
@Order(1)
@Qualifier("main")
public class FileMenu extends JMenu {

  public FileMenu(@Qualifier("file") Map<String, SmartAction> actions) {
    super("File");
    Menus.addMenuItems(this, actions);
  }
}
