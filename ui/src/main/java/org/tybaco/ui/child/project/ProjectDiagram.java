package org.tybaco.ui.child.project;

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

import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.tybaco.ui.lib.context.UIComponent;

import java.util.Random;

import static javafx.scene.input.MouseEvent.MOUSE_MOVED;
import static javafx.scene.input.ScrollEvent.SCROLL;

@UIComponent
public class ProjectDiagram extends ScrollPane {

  private final Group content = new Group();
  private final Group connectors = new Group();
  private final StackPane stackPane = new StackPane(connectors, content);
  private final Group zoomGroup = new Group(stackPane);
  private final StackPane contentGroup = new StackPane(zoomGroup);

  private boolean zoom = false;
  private double zy;

  public ProjectDiagram() {
    setContent(contentGroup);
    setFitToHeight(true);
    setFitToWidth(true);
    setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
    setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
    setPannable(true);
    contentGroup.addEventHandler(MOUSE_MOVED, this::onMouseMove);
    contentGroup.addEventHandler(SCROLL, e -> onScroll(e.getDeltaY(), e.getX(), e.getY()));
    var r = new Random(0L);
    for (int i = 0; i < 100; i++) {
      var box = new Rectangle(r.nextDouble() * 1000d, r.nextDouble() * 1000d, r.nextDouble() * 100d, r.nextDouble() * 100d);
      box.setFill(new Color(r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble()));
      content.getChildren().add(box);
    }
  }

  private void onMouseMove(MouseEvent event) {
    if (event.isControlDown()) {
      if (zoom) {
        onScroll(zy - event.getY(), event.getX(), event.getY());
      } else {
        zoom = true;
      }
      zy = event.getY();
      event.consume();
    } else {
      zoom = false;
    }
  }

  private void onScroll(double delta, double px, double py) {
    var zoomFactor = Math.exp(delta * 0.01);
    var innerBounds = zoomGroup.getLayoutBounds();
    var viewportBounds = getViewportBounds();
    var x = getHvalue() * (innerBounds.getWidth() - viewportBounds.getWidth());
    var y = getVvalue() * (innerBounds.getHeight() - viewportBounds.getHeight());
    content.setScaleX(content.getScaleX() * zoomFactor);
    content.setScaleY(content.getScaleY() * zoomFactor);
    layout();
    var pos = content.parentToLocal(zoomGroup.parentToLocal(px, py));
    var scrollPos = content.getLocalToParentTransform().deltaTransform(pos.multiply(zoomFactor - 1d));
    var newInnerBounds = zoomGroup.getBoundsInLocal();
    setHvalue((x + scrollPos.getX()) / (newInnerBounds.getWidth() - viewportBounds.getWidth()));
    setVvalue((y + scrollPos.getY()) / (newInnerBounds.getHeight() - viewportBounds.getHeight()));
  }
}
