package org.tybaco.ui.child.project.diagram;

import jakarta.annotation.PostConstruct;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Affine;

import static java.lang.Math.exp;
import static javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER;
import static javafx.scene.input.MouseEvent.MOUSE_DRAGGED;
import static javafx.scene.input.MouseEvent.MOUSE_MOVED;
import static javafx.scene.input.ScrollEvent.SCROLL;
import static javafx.scene.input.ZoomEvent.ZOOM;

abstract class AbstractProjectDiagram extends ScrollPane {

  private static final double ZOOM_PRECISION = 0.01;
  private static final double ROTATE_PRECISION = 0.1;

  protected final Pane blocks = new Pane();
  protected final Pane connectors = new Pane();
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
  }

  @PostConstruct
  protected void init() {
    content.addEventHandler(SCROLL, this::onScroll);
    content.addEventHandler(MOUSE_DRAGGED, this::onMouseDrag);
    content.addEventHandler(MOUSE_MOVED, this::onMouseMove);
    content.addEventHandler(ZOOM, e -> zoom(e.getZoomFactor(), e.getX(), e.getY()));
  }

  protected void onScroll(ScrollEvent event) {
    if (event.isControlDown()) {
      zoom(exp(event.getDeltaY() * ZOOM_PRECISION), event.getX(), event.getY());
    } else if (event.isAltDown()) {
      rotate(event.getDeltaX(), event.getX(), event.getY());
    } else {
      transform.appendTranslation(event.getDeltaX(), event.getDeltaY());
    }
  }

  protected void onMouseMove(MouseEvent event) {
    if (event.isControlDown()) {
      zoom(exp((my - event.getY()) * ZOOM_PRECISION), mx, my);
    } else if (event.isShiftDown()) {
      pan(mx, my, event.getX(), event.getY());
    } else if (event.isAltDown()) {
      rotate(event.getX() - mx, event.getX(), event.getY());
    }
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

  private void zoom(double zoomFactor, double px, double py) {
    var local = layers.parentToLocal(px, py);
    transform.appendScale(zoomFactor, zoomFactor, local);
  }

  private void rotate(double dx, double x, double y) {
    transform.appendRotation(dx * ROTATE_PRECISION, x, y);
  }

  public void resetTransform() {
    transform.setToIdentity();
  }
}
