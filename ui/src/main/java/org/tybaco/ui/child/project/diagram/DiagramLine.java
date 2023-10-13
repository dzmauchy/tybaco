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
import javafx.beans.*;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import org.tybaco.ui.model.Link;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.stream.Stream;

import static org.tybaco.ui.child.project.diagram.DiagramCalculations.boundsIn;

public class DiagramLine extends Group {

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
    var xs = outBounds.getMaxX();
    var ys = outBounds.getCenterY();
    var xe = inBounds.getMinX();
    var ye = inBounds.getCenterY();
    if (xs < xe - 50d) {
      var d = (xe - xs) / 5d;
      var shape = new Line2D.Double(xs + 3d, ys, xe - 3d, ye);
      if (canBeDrawn(input, output, shape)) {
        var elems = new ArrayList<PathElement>(2);
        elems.add(new MoveTo(xs, ys));
        elems.add(new CubicCurveTo(xs + d, ys, xe - d, ye, xe, ye));
        path.getElements().setAll(elems);
        link.separated.set(false);
        return;
      }
    }
    link.separated.set(true);
    path.getElements().clear();
  }

  private boolean canBeDrawn(DiagramBlockInput input, DiagramBlockOutput output, Shape  ... shapes) {
    return Stream.concat(blocks(input, output), connectors(input, output))
      .parallel()
      .map(b -> new Rectangle2D.Double(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight()))
      .noneMatch(b -> Arrays.stream(shapes).anyMatch(s -> s.intersects(b)));
  }

  private Stream<Bounds> blocks(DiagramBlockInput input, DiagramBlockOutput output) {
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
