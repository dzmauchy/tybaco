package org.tybaco.ui.child.project.diagram;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import org.tybaco.ui.model.Link;

import java.util.LinkedList;

public class DiagramLine extends Path {

  final InvalidationListener invalidationListener = this::onUpdate;
  final Link link;

  public DiagramLine(Link link) {
    this.link = link;
    setStrokeWidth(2d);
    setStroke(Color.WHITE);
    setStrokeLineJoin(StrokeLineJoin.ROUND);
    link.inpSpot.addListener(invalidationListener);
    link.outSpot.addListener(invalidationListener);
    parentProperty().addListener((k, ov, nv) -> {
      if (nv == null) {
        link.inpSpot.removeListener(invalidationListener);
        link.outSpot.removeListener(invalidationListener);
      }
    });
  }

  private void onUpdate(Observable o) {
    var elements = new LinkedList<PathElement>();
    var p1 = link.outSpot.get();
    var p2 = link.inpSpot.get();
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
        if (dy > 200d) {
          var mu = 600 / dy;
          elements.add(new CubicCurveTo(
            p1.getX() + dx * mu, p1.getY() + dy / 2d,
            p2.getX() - dx * mu, p2.getY() - dy / 2d,
            p2.getX(), p2.getY()
          ));
        } else {
          elements.add(new CubicCurveTo(
            p1.getX() + dx / 2, p1.getY() + dy / 6d,
            p1.getX() + dx * 2d, p2.getY() + dy / 3d,
            p1.getX() - dx / 2, p2.getY() + dy / 3d
          ));
          elements.add(new CubicCurveTo(
            p2.getX() - dx, p2.getY() + dy / 3d,
            p2.getX() - dx / 2, p2.getY() + dy / 8d,
            p2.getX(), p2.getY()
          ));
        }
      } else {
        elements.add(new LineTo(p2.getX(), p2.getY()));
      }
    }
    getElements().setAll(elements);
  }
}
