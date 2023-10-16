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
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.StrokeLineJoin;
import org.tybaco.ui.child.project.diagram.Diagram;
import org.tybaco.ui.child.project.diagram.DiagramCalculations;
import org.tybaco.ui.model.Link;
import org.tybaco.ui.util.ArrayBasedCurveDivider;

import java.util.stream.Stream;

public class DiagramLine extends Group {

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
    var ib = link.inBounds.get();
    var ob = link.outBounds.get();
    if (ib != null && ob != null) {
      onUpdate(ib, ob);
    }
  }

  private void onUpdate(Bounds ib, Bounds ob) {
    var context = new LineContext(ib, ob);
    for (var type : LineType.LINE_TYPES) {
      var line = LineType.createLine(type, this, context);
      if (line.tryApply()) {
        return;
      }
    }
    path.setVisible(false);
  }

  boolean tryApply(Line line, double cx1, double cy1, double cx2, double cy2) {
    var divider = line.getDivider();
    var context = line.getContext();
    double xs = context.xs(), ys = context.ys(), xe = context.xe(), ye = context.ye();
    divider.divide(xs, ys, cx1, cy1, cx2, cy2, xe, ye);
    if (blocks().noneMatch(divider::intersects)) {
      divider.setCurve(path, xs, ys, cx1, cy1, cx2, cy2, xe, ye);
      path.setVisible(true);
      return true;
    } else {
      return false;
    }
  }

  boolean apply(ArrayBasedCurveDivider divider, double x1, double y1, double cx1, double cy1, double cx2, double cy2, double x2, double y2) {
    divider.setCurve(path, x1, y1, cx1, cy1, cx2, cy2, x2, y2);
    return true;
  }

  private Stream<Bounds> blocks() {
    var blocksBase = diagram.blocks;
    return blocksBase.getChildren().stream().map(n -> DiagramCalculations.boundsIn(blocksBase, n));
  }
}
