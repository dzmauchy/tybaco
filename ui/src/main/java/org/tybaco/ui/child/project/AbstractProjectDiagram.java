package org.tybaco.ui.child.project;

import jakarta.annotation.PostConstruct;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;

import java.util.Random;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER;
import static javafx.scene.input.ScrollEvent.SCROLL;

abstract class AbstractProjectDiagram extends ScrollPane {

  protected final Group blocks = new Group();
  protected final Group connectors = new Group();
  protected final StackPane layers = new StackPane(connectors, blocks);
  protected final StackPane content = new StackPane(layers);
  protected final Affine transform = new Affine();

  private double mx;
  private double my;

  AbstractProjectDiagram() {
    setContent(content);
    setFitToHeight(true);
    setFitToWidth(true);
    setHbarPolicy(NEVER);
    setVbarPolicy(NEVER);
    layers.getTransforms().add(transform);
    var r = new Random(0L);
    for (int i = 0; i < 100; i++) {
      var box = new Rectangle(r.nextDouble() * 1000d, r.nextDouble() * 1000d, r.nextDouble() * 100d, r.nextDouble() * 100d);
      box.setFill(new Color(r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble()));
      blocks.getChildren().add(box);
    }
  }

  @PostConstruct
  private void init() {
    content.addEventHandler(SCROLL, this::onScroll);
    content.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDrag);
    content.addEventHandler(MouseEvent.MOUSE_MOVED, this::onMouseMove);
  }

  protected void onScroll(ScrollEvent event) {
    event.consume();
    zoom(delta(event), event.getX(), event.getY());
  }

  private double delta(ScrollEvent event) {
    return abs(event.getDeltaX()) > abs(event.getDeltaY()) ? -event.getDeltaX() : event.getDeltaY();
  }

  protected void onMouseMove(MouseEvent event) {
    mx = event.getX();
    my = event.getY();
  }

  protected void onMouseDrag(MouseEvent event) {
    pan(mx, my, event.getX(), event.getY());
    mx = event.getX();
    my = event.getY();
  }

  private void pan(double ox, double oy, double px, double py) {
    var o = layers.parentToLocal(ox, oy);
    var p = layers.parentToLocal(px, py);
    transform.appendTranslation(p.getX() - o.getX(), p.getY() - o.getY());
  }

  private void zoom(double delta, double px, double py) {
    var zoomFactor = Math.exp(delta * 0.01);
    var local = layers.parentToLocal(px, py);
    transform.appendScale(zoomFactor, zoomFactor, local);
  }

  public void resetTransform() {
    transform.setToIdentity();
  }
}
