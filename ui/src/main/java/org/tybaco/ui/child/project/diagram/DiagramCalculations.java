package org.tybaco.ui.child.project.diagram;

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

import javafx.beans.Observable;
import javafx.beans.binding.ObjectBinding;
import javafx.geometry.Bounds;
import javafx.scene.Node;

import java.util.LinkedList;

import static javafx.beans.binding.Bindings.createObjectBinding;

interface DiagramCalculations {

  static ObjectBinding<Bounds> boundsBinding(Node base, Node current) {
    var observables = new LinkedList<Observable>();
    observables.add(current.boundsInLocalProperty());
    for (var c = current; c != base; c = c.getParent()) {
      observables.add(c.localToParentTransformProperty());
    }
    return createObjectBinding(() -> boundsIn(base, current), observables.toArray(Observable[]::new));
  }

  static Bounds boundsIn(Node base, Node current) {
    var b = current.getBoundsInLocal();
    for (var c = current; c != base; c = c.getParent()) {
      if (c == null) return b;
      b = c.getLocalToParentTransform().transform(b);
    }
    return b;
  }
}
