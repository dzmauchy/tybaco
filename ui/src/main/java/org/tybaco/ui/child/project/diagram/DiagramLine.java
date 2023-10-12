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
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import org.tybaco.ui.model.Link;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.IdentityHashMap;

import static java.lang.Boolean.TRUE;
import static org.tybaco.ui.child.project.diagram.DiagramCalculations.boundsIn;

public class DiagramLine extends Group {

  final InvalidationListener invalidationListener = this::onUpdate;
  final Link link;
  final Path path = new Path();

  public DiagramLine(Link link) {
    this.link = link;
    getChildren().add(path);
    path.setStrokeWidth(2d);
    path.setStroke(Color.WHITE);
    path.setStrokeLineJoin(StrokeLineJoin.ROUND);
    link.lineEnabled.addListener((o, ov, nv) -> {
      if (nv) {
        var input = link.input.get();
        var output = link.output.get();
        var wl = new WeakInvalidationListener(invalidationListener);
        var base = input.block.diagram.blocks;
        var map = new IdentityHashMap<Observable, Boolean>();
        map.put(input.boundsInLocalProperty(), TRUE);
        for (Node c = input; c != base; c = c.getParent()) map.put(c.localToParentTransformProperty(), TRUE);
        map.put(output.boundsInLocalProperty(), TRUE);
        for (Node c = output; c != base; c = c.getParent()) map.put(c.localToParentTransformProperty(), TRUE);
        map.forEach((k, v) -> k.addListener(wl));
        setVisible(true);
      } else {
        setVisible(false);
      }
    });
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
      var canBeDrawn = blocksBase.getChildren().stream()
        .map(n -> boundsIn(blocksBase, n))
        .map(b -> new Rectangle2D.Double(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight()))
        .noneMatch(new Line2D.Double(xs + 3d, ys, xe - 3d, ye)::intersects);
      if (canBeDrawn) {
        var d = (xe - xs) / 5d;
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
}
