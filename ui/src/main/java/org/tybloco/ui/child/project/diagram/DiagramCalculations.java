package org.tybloco.ui.child.project.diagram;

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

import javafx.geometry.Bounds;
import javafx.scene.Node;

public interface DiagramCalculations {

  static Bounds boundsIn(Node base, Node current) {
    var b = current.getBoundsInLocal();
    for (var c = current; c != base; c = c.getParent()) {
      if (c == null) return b;
      var t = c.getLocalToParentTransform();
      if (t == null) return b;
      b = t.transform(b);
    }
    return b;
  }
}
