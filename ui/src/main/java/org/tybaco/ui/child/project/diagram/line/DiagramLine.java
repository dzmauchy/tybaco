package org.tybaco.ui.child.project.diagram.line;

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

import javafx.beans.*;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import org.tybaco.ui.child.project.diagram.Diagram;
import org.tybaco.ui.child.project.diagram.DiagramCalculations;
import org.tybaco.ui.model.Link;
import org.tybaco.ui.util.ArrayBasedCurveDivider;

import java.util.stream.Stream;

public class DiagramLine extends Group {

  static final boolean DEBUG = false;

  private final InvalidationListener boundsInvalidationListener = this::onUpdate;
  public final Diagram diagram;
  public final Link link;
  final CubicCurve path = new CubicCurve();

  public DiagramLine(Diagram diagram, Link link) {
    this.diagram = diagram;
    this.link = link;
    visibleProperty().bind(link.input.isNotNull().and(link.output.isNotNull()).and(link.inBounds.isNotNull()).and(link.outBounds.isNotNull()));
    getChildren().add(path);
    path.setStrokeWidth(2d);
    path.setStroke(Color.WHITE);
    path.setStrokeLineJoin(StrokeLineJoin.ROUND);
    path.setFill(null);
    initialize();
  }

  private void initialize() {
    var wil = new WeakInvalidationListener(boundsInvalidationListener);
    link.inBounds.addListener(wil);
    link.outBounds.addListener(wil);
    diagram.diagramBlockBoundsObserver.addListener(wil);
    onUpdate(null);
  }

  private void onUpdate(Observable o) {
    if (!isVisible()) {
      return;
    }
    if (DEBUG) {
      diagram.debugNodes.getChildren().removeIf(n -> {
        if (n.getUserData() instanceof Link l) {
          return l.output.get() == null || l.input.get() == null || l == link;
        } else {
          return true;
        }
      });
    }
    onUpdate(link.inBounds.get(), link.outBounds.get());
  }

  private void onUpdate(Bounds inBounds, Bounds outBounds) {
    for (var type : LineType.LINE_TYPES) {
      var line = LineType.createLine(type, this);
      if (line.tryApply(inBounds, outBounds)) {
        return;
      }
    }
    path.setVisible(false);
  }

  boolean tryApply(ArrayBasedCurveDivider divider, double x1, double y1, double cx1, double cy1, double cx2, double cy2, double x2, double y2) {
    var applier = divider.divide(x1, y1, cx1, cy1, cx2, cy2, x2, y2);
    if (blocks().noneMatch(divider::intersects)) {
      applier.accept(path);
      path.setVisible(true);
      return true;
    } else {
      return false;
    }
  }

  private Stream<Bounds> blocks() {
    var blocksBase = diagram.blocks;
    return blocksBase.getChildren().stream().map(n -> DiagramCalculations.boundsIn(blocksBase, n));
  }

  private void debug(Bounds b, Color color) {
    var r = new Rectangle(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
    r.setFill(color);
    r.setUserData(link);
    diagram.debugNodes.getChildren().add(r);
  }
}
