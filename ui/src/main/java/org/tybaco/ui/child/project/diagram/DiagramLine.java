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

import java.util.LinkedList;

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
    if (p1 == null || p2 == null) return;
    var elements = new LinkedList<PathElement>();
    elements.add(new MoveTo(p1.getX(), p1.getY()));
    if (p1.getX() < p2.getX()) {
      var d = (p2.getX() - p1.getX()) / 5d;
      elements.add(new CubicCurveTo(
        p1.getX() + d, p1.getY(),
        p2.getX() - d, p2.getY(),
        p2.getX(), p2.getY()
      ));
    } else {
      var dx = Math.abs(p2.getX() - p1.getX());
      var dy = Math.abs(p2.getY() - p1.getY());
      if (p2.getY() > p1.getY()) {
        elements.add(new CubicCurveTo(
          p1.getX() + dx / 2d, p1.getY() + dy / 6d,
          p1.getX() + dx * 2d, p2.getY() + dy / 3d,
          p1.getX() - dx / 3d, p2.getY() + dy
        ));
        elements.add(new CubicCurveTo(
          p2.getX() - dx, p2.getY() + dy * 1.333d,
          p2.getX() - dx / 2d, p2.getY() + dy / 8d,
          p2.getX(), p2.getY()
        ));
      } else {
        elements.add(new LineTo(p2.getX(), p2.getY()));
      }
    }
    path.getElements().setAll(elements);
  }
}
