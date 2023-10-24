package org.tybloco.ui.main;

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

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tybloco.editors.action.Action;

import java.util.List;

@Component
public class MainMenuBar extends MenuBar {

  public MainMenuBar(@Qualifier("mainMenu") List<Action> actions) {
    super(actions.stream().map(Action::toSmartMenu).toArray(Menu[]::new));
  }
}
