package org.tybaco.ui.lib.window;

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

import java.awt.*;
import java.util.Optional;

public final class Windows {

  private Windows() {
  }

  public static <W extends Window> Optional<W> findWindow(Class<W> windowType) {
    for (var window : Window.getWindows()) {
      if (windowType.isInstance(window)) {
        return Optional.of(windowType.cast(window));
      }
    }
    return Optional.empty();
  }
}
