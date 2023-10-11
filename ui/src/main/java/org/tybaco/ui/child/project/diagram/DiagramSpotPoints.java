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
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;

import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;

public interface DiagramSpotPoints {

  static ObjectBinding<Point2D> spotPointBinding(Node base, Node current, Function<Bounds, Point2D> func) {
    var observables = new LinkedList<Observable>();
    observables.add(current.boundsInLocalProperty());
    for (var c = current; c != base; c = c.getParent()) {
      observables.add(c.localToParentTransformProperty());
    }
    return Bindings.createObjectBinding(() -> {
      var bounds = current.getBoundsInLocal();
      var point = func.apply(bounds);
      for (var c = current; c != base; c = c.getParent()) {
        point = c.getLocalToParentTransform().transform(point);
      }
      return point;
    }, observables.toArray(Observable[]::new));
  }

  static void installSpotPointMonitoring(Node base, Node current, Function<Bounds, Point2D> func, Consumer<ObjectBinding<Point2D>> consumer) {
    var scene = current.getScene();
    if (scene != null) {
      consumer.accept(spotPointBinding(base, current, func));
    } else {
      current.sceneProperty().addListener(new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Scene> observable, Scene oldValue, Scene newValue) {
          current.sceneProperty().removeListener(this);
          consumer.accept(spotPointBinding(base, current, func));
        }
      });
    }
  }

  static Point2D outputSpot(Bounds bounds) {
    return new Point2D(bounds.getMaxX(), bounds.getCenterY());
  }

  static Point2D inputSpot(Bounds bounds) {
    return new Point2D(bounds.getMinX(), bounds.getCenterY());
  }
}
