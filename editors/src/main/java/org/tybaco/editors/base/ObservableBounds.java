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

import javafx.beans.*;
import javafx.beans.value.*;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;

public final class ObservableBounds extends ObservableValueBase<Bounds> {

  private final Node base;
  private final Node current;
  private final InvalidationListener listener = this::update;
  private final WeakInvalidationListener weakListener = new WeakInvalidationListener(listener);
  private boolean completed;

  public ObservableBounds(Node base, Node current) {
    this.base = base;
    this.current = current;
    current.boundsInLocalProperty().addListener(weakListener);
    addListener(current);
  }

  private void addListener(Node node) {
    for (var c = node; c != base; ) {
      c.localToParentTransformProperty().addListener(weakListener);
      if (c.getParent() == null) {
        var parentProperty = c.parentProperty();
        parentProperty.addListener(new ChangeListener<>() {
          @Override
          public void changed(ObservableValue<? extends Parent> observable, Parent oldValue, Parent newValue) {
            if (newValue != null) {
              addListener(newValue);
              parentProperty.removeListener(this);
            }
          }
        });
        return;
      } else {
        c = c.getParent();
      }
    }
    completed = true;
    update(null);
  }

  private void update(Observable observable) {
    if (completed) {
      fireValueChangedEvent();
    }
  }

  @Override
  public Bounds getValue() {
    var b = current.getBoundsInLocal();
    for (var c = current; c != base; c = c.getParent()) {
      if (c == null) {
        return b;
      }
      b = c.getLocalToParentTransform().transform(b);
    }
    return b;
  }
}
