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

import static java.lang.Math.max;
import static java.lang.Math.min;

public class DiagramLine extends Group {

  static final double SAFE_DIST = 3d;
  static final double STEP = 30d;
  static final boolean DEBUG = false;
  static final ArrayBasedCurveDivider D4 = new ArrayBasedCurveDivider(4);
  static final ArrayBasedCurveDivider D5 = new ArrayBasedCurveDivider(5);

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
    if (new SimpleLine(this).trySimpleLine(inBounds, outBounds))
      return;
    /**
    if (tryLineOI(inBounds, outBounds))
      return;
    if (tryOuter(inBounds, outBounds))
      return;
     **/
    path.setVisible(false);
  }

  private boolean tryLineOI(Bounds ib, Bounds ob) {
    var xs = (float) ob.getMaxX();
    var ys = (float) ob.getCenterY();
    var xe = (float) ib.getMinX();
    var ye = (float) ib.getCenterY();
    var ub = ib.getMinY() < ob.getMinY() ? ib : ob;
    var lb = ib.getMinY() < ob.getMinY() ? ob : ib;
    if (lb.getMinY() > ub.getMaxY()) {
      var gapY = (float) (lb.getMinY() - ub.getMaxY());
      if (gapY > 50f) {
        var maxX = (float) (max(lb.getMaxX(), ub.getMaxX()) + ub.getWidth() * 13d);
        var ry = (float) (ub.getMinY() + gapY / 3f);
        var minX = (float) (min(lb.getMinX(), ub.getMinX()) - lb.getWidth() * 13d);
        var ly = (float) (lb.getMinY() - gapY / 3f);
        return tryApply(D4, xs + SAFE_DIST, ys, maxX, ry, minX, ly, xe - SAFE_DIST, ye);
      }
    }
    return false;
  }

  private boolean tryOuter(Bounds ib, Bounds ob) {
    var xs = (float) ob.getMaxX();
    var ys = (float) ob.getCenterY();
    var xe = (float) ib.getMinX();
    var ye = (float) ib.getCenterY();
    {
      var cx1 = xs + ob.getWidth();
      var cy1 = Math.max(ib.getMaxY(), ob.getMaxY()) + ob.getHeight() + ib.getHeight();
      var cx2 = xe - ib.getWidth();
      var cy2 = cy1 - ob.getHeight();
      if (tryApply(D4, xs + SAFE_DIST, ys, cx1, cy1, cx2, cy2, xe - SAFE_DIST, ye)) {
        return true;
      }
    }
    {
      var cx1 = xs + ob.getWidth();
      var cy1 = Math.min(ib.getMinY(), ob.getMinY()) - ob.getHeight() - ib.getHeight();
      var cx2 = xe - ib.getWidth();
      var cy2 = cy1 + ob.getHeight();
      return tryApply(D4, xs + SAFE_DIST, ys, cx1, cy1, cx2, cy2, xe - SAFE_DIST, ye);
    }
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
