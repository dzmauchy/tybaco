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

import com.sun.javafx.geom.*;
import com.sun.javafx.geom.Shape;
import javafx.beans.Observable;
import javafx.beans.*;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import org.tybaco.ui.model.Link;

import java.util.*;
import java.util.stream.Stream;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.tybaco.ui.child.project.diagram.DiagramCalculations.boundsIn;

public class DiagramLine extends Group {

  private static final float SAFE_DIST = 2f;

  private final InvalidationListener boundsInvalidationListener = this::onUpdate;
  private final InvalidationListener connectorsInvalidationListener = this::onUpdateConnectors;
  final Link link;
  final Path path = new Path();

  public DiagramLine(Link link) {
    this.link = link;
    getChildren().add(path);
    path.setStrokeWidth(2d);
    path.setStroke(Color.WHITE);
    path.setStrokeLineJoin(StrokeLineJoin.ROUND);
    initialize();
  }

  private void initialize() {
    var cwl = new WeakInvalidationListener(connectorsInvalidationListener);
    link.input.addListener(cwl);
    link.output.addListener(cwl);
    cwl.invalidated(null);
  }

  private void onUpdateConnectors(Observable o) {
    var input = link.input.get();
    var output = link.output.get();
    if (input == null || output == null) {
      setVisible(false);
      return;
    }
    var wl = new WeakInvalidationListener(boundsInvalidationListener);
    var base = input.block.diagram.blocks;
    var map = new IdentityHashMap<Observable, WeakInvalidationListener>();
    map.put(input.boundsInLocalProperty(), wl);
    for (Node c = input; c != base; c = c.getParent()) map.put(c.localToParentTransformProperty(), wl);
    map.put(output.boundsInLocalProperty(), wl);
    for (Node c = output; c != base; c = c.getParent()) map.put(c.localToParentTransformProperty(), wl);
    map.forEach(Observable::addListener);
    setVisible(true);
    onUpdate(null);
  }

  private void onUpdate(Observable o) {
    var input = link.input.get();
    var output = link.output.get();
    if (input == null || output == null) return;
    var blocksBase = input.block.diagram.blocks;
    var outBounds = boundsIn(blocksBase, output);
    var inBounds = boundsIn(blocksBase, input);
    var xs = (float) outBounds.getMaxX();
    var ys = (float) outBounds.getCenterY();
    var xe = (float) inBounds.getMinX();
    var ye = (float) inBounds.getCenterY();
    if (xs < xe) {
      if (xe - xs >= 50f) {
        var dx = (xe - xs) / 5f;
        var shape = new CubicCurve2D(xs + SAFE_DIST, ys, xs + dx, ys, xe - dx, ye, xe - SAFE_DIST, ye);
        if (canBeDrawn(input, output, shape)) {
          apply(shape);
          return;
        }
      }
    }
    if (inBounds.getMinY() > outBounds.getMaxY()) {
      var gapY = (float) (inBounds.getMinY() - outBounds.getMaxY());
      if (gapY > 50f) {
        var maxX = (float) (max(inBounds.getMaxX(), outBounds.getMaxX()) + outBounds.getWidth() * Math.PI);
        var ry = (float) (outBounds.getMinY() + gapY / 4f);
        var minX = (float) (min(inBounds.getMinX(), outBounds.getMinX()) - inBounds.getWidth() * Math.PI);
        var ly = (float) (inBounds.getMinY() - gapY / 4f);
        var shape = new CubicCurve2D(xs + SAFE_DIST, ys, maxX, ry, minX, ly, xe - SAFE_DIST, ye);
        if (canBeDrawn(input, output, shape)) {
          apply(shape);
          return;
        }
      }
    }
    link.separated.set(true);
    path.getElements().clear();
  }

  private void apply(Shape... shapes) {
    var elems = new LinkedList<PathElement>();
    for (var shape : shapes) {
      switch (shape) {
        case Line2D l -> {
          elems.add(new MoveTo(l.x1, l.y1));
          elems.add(new LineTo(l.x2, l.y2));
        }
        case CubicCurve2D c -> {
          elems.add(new MoveTo(c.x1, c.y1));
          elems.add(new CubicCurveTo(c.ctrlx1, c.ctrly1, c.ctrlx2, c.ctrly2, c.x2, c.y2));
        }
        case QuadCurve2D c -> {
          elems.add(new MoveTo(c.x1, c.y1));
          elems.add(new QuadCurveTo(c.ctrlx, c.ctrly, c.x2, c.y2));
        }
        default -> {}
      }
    }
    path.getElements().setAll(elems);
    link.separated.set(false);
  }

  private boolean canBeDrawn(DiagramBlockInput input, DiagramBlockOutput output, Shape... shapes) {
    return Stream.concat(blocks(input), connectors(input, output))
      .parallel()
      .noneMatch(b -> Arrays.stream(shapes).anyMatch(s ->
        s.intersects((float) b.getMinX(), (float) b.getMinY(), (float) b.getWidth(), (float) b.getHeight())
      ));
  }

  private Stream<Bounds> blocks(DiagramBlockInput input) {
    var blocksBase = input.block.diagram.blocks;
    return blocksBase.getChildren().stream()
      .parallel()
      .filter(Node::isVisible)
      .map(n -> boundsIn(blocksBase, n));
  }

  private Stream<Bounds> connectors(DiagramBlockInput input, DiagramBlockOutput output) {
    var connectorsBase = input.block.diagram.connectors;
    return connectorsBase.getChildren().stream()
      .parallel()
      .filter(n -> !(n instanceof DiagramLine))
      .filter(Node::isVisible)
      .filter(n -> checkCompanion(n, input, output))
      .map(n -> boundsIn(connectorsBase, n));
  }

  private boolean checkCompanion(Node node, DiagramBlockInput input, DiagramBlockOutput output) {
    return switch (node) {
      case DiagramBlockInputCompanion c -> c.input != input;
      case DiagramBlockOutputCompanion c -> c.output != output;
      default -> true;
    };
  }
}
