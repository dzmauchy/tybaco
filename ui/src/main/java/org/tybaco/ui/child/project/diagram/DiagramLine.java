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

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import org.tybaco.ui.model.Link;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

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
    parentProperty().addListener((k, ov, nv) -> {
      if (nv == null) {
        link.inpSpot.removeListener(invalidationListener);
        link.outSpot.removeListener(invalidationListener);
      } else {
        link.inpSpot.addListener(invalidationListener);
        link.outSpot.addListener(invalidationListener);
      }
    });
  }

  private void onUpdate(Observable o) {
    var p1 = link.outSpot.get();
    var p2 = link.inpSpot.get();
    var out = link.output.get();
    if (p1 == null || p2 == null) return;
    if (p1.getX() < p2.getX() - 50d) {
      var blocksBase = out.block.diagram.blocks;
      var canBeDrawn = blocksBase.getChildren().stream()
        .parallel()
        .map(n -> boundsIn(blocksBase, n))
        .map(b -> new Rectangle2D.Double(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight()))
        .noneMatch(new Line2D.Double(p1.getX(), p1.getY(), p2.getX(), p2.getY())::intersects);
      if (canBeDrawn) {
        var d = (p2.getX() - p1.getX()) / 5d;
        var elems = new ArrayList<PathElement>(2);
        elems.add(new MoveTo(p1.getX(), p1.getY()));
        elems.add(new CubicCurveTo(p1.getX() + d, p1.getY(), p2.getX() - d, p2.getY(), p2.getX(), p2.getY()));
        path.getElements().setAll(elems);
        link.separated.set(false);
        return;
      }
    }
    link.separated.set(true);
    path.getElements().clear();
  }
}
