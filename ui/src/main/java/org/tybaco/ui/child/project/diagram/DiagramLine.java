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

import javafx.beans.*;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.*;
import org.tybaco.ui.model.Link;
import java.awt.Shape;

import java.awt.geom.*;
import java.util.LinkedList;
import java.util.stream.Stream;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.tybaco.ui.child.project.diagram.DiagramCalculations.boundsIn;

public class DiagramLine extends Group {

  private static final float SAFE_DIST = 3f;
  private static final boolean DEBUG = false;

  private final InvalidationListener boundsInvalidationListener = this::onUpdate;
  final Diagram diagram;
  final Link link;
  final Path path = new Path();

  public DiagramLine(Diagram diagram, Link link) {
    this.diagram = diagram;
    this.link = link;
    visibleProperty().bind(link.input.isNotNull().and(link.output.isNotNull()).and(link.inBounds.isNotNull()).and(link.outBounds.isNotNull()));
    getChildren().add(path);
    path.setStrokeWidth(2d);
    path.setStroke(Color.WHITE);
    path.setStrokeLineJoin(StrokeLineJoin.ROUND);
    initialize();
  }

  private void initialize() {
    var wil = new WeakInvalidationListener(boundsInvalidationListener);
    link.inBounds.addListener(wil);
    link.outBounds.addListener(wil);
    onUpdate(null);
  }

  private void onUpdate(Observable o) {
    if (!isVisible()) {
      path.getElements().clear();
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
    if (trySimpleLine(inBounds, outBounds))
      return;
    if (tryLineOI(inBounds, outBounds))
      return;
    path.getElements().clear();
  }

  private boolean trySimpleLine(Bounds ib, Bounds ob) {
    var xs = (float) ob.getMaxX();
    var ys = (float) ob.getCenterY();
    var xe = (float) ib.getMinX();
    var ye = (float) ib.getCenterY();
    if (xs < xe) {
      if (xe - xs >= 50f) {
        var dx = (xe - xs) / 5f;
        var shape = new CubicCurve2D.Double(xs + SAFE_DIST, ys, xs + dx, ys, xe - dx, ye, xe - SAFE_DIST, ye);
        return tryApply(shape);
      }
    }
    return false;
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
        var shape = new CubicCurve2D.Double(xs + SAFE_DIST, ys, maxX, ry, minX, ly, xe - SAFE_DIST, ye);
        return tryApply(shape);
      }
    }
    return false;
  }

  private void apply(Shape... shapes) {
    var elems = new LinkedList<PathElement>();
    for (var shape : shapes) {
      switch (shape) {
        case Line2D.Double l -> {
          elems.add(new MoveTo(l.x1, l.y1));
          elems.add(new LineTo(l.x2, l.y2));
        }
        case CubicCurve2D.Double c -> {
          elems.add(new MoveTo(c.x1, c.y1));
          elems.add(new CubicCurveTo(c.ctrlx1, c.ctrly1, c.ctrlx2, c.ctrly2, c.x2, c.y2));
        }
        case QuadCurve2D.Double c -> {
          elems.add(new MoveTo(c.x1, c.y1));
          elems.add(new QuadCurveTo(c.ctrlx, c.ctrly, c.x2, c.y2));
        }
        default -> {}
      }
    }
    path.getElements().setAll(elems);
  }

  private boolean tryApply(Shape... shapes) {
    var needsApply = blocks().noneMatch(b -> {
      for (var s : shapes) {
        if (s.intersects(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight())) {
          return true;
        }
      }
      return false;
    });
    if (needsApply) {
      apply(shapes);
    }
    return needsApply;
  }

  private Stream<Bounds> blocks() {
    var blocksBase = diagram.blocks;
    var stream = blocksBase.getChildren().stream();
    return (DEBUG ? stream : stream.parallel()).map(n -> boundsIn(blocksBase, n));
  }

  private void debug(Bounds b, Color color) {
    var r = new Rectangle(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
    r.setFill(color);
    r.setUserData(link);
    diagram.debugNodes.getChildren().add(r);
  }
}
