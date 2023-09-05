package org.tybaco.ui.child.project;

import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.tybaco.ui.lib.context.UIComponent;

import java.util.Random;

import static javafx.scene.input.ScrollEvent.SCROLL;

@UIComponent
public class ProjectDiagram extends ScrollPane {

  private final Group content = new Group();
  private final Group connectors = new Group();
  private final StackPane stackPane = new StackPane(connectors, content);
  private final Group zoomGroup = new Group(stackPane);
  private final StackPane contentGroup = new StackPane(zoomGroup);

  public ProjectDiagram() {
    setContent(contentGroup);
    setFitToHeight(true);
    setFitToWidth(true);
    setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
    setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
    setPannable(true);
    contentGroup.addEventHandler(SCROLL, e -> {
      onScroll(e.getDeltaY(), new Point2D(e.getX(), e.getY()));
    });
    var r = new Random(0L);
    for (int i = 0; i < 100; i++) {
      var box = new Rectangle(r.nextDouble() * 1000d, r.nextDouble() * 1000d, r.nextDouble() * 100d, r.nextDouble() * 100d);
      box.setFill(new Color(r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble()));
      content.getChildren().add(box);
    }
  }

  private void onScroll(double delta, Point2D p) {
    var zoomFactor = Math.exp(delta * 0.01);
    var innerBounds = zoomGroup.getLayoutBounds();
    var viewportBounds = getViewportBounds();
    var x = getHvalue() * (innerBounds.getWidth() - viewportBounds.getWidth());
    var y = getVvalue() * (innerBounds.getHeight() - viewportBounds.getHeight());
    content.setScaleX(content.getScaleX() * zoomFactor);
    content.setScaleY(content.getScaleY() * zoomFactor);
    layout();
    var pos = content.parentToLocal(zoomGroup.parentToLocal(p));
    var scrollPos = content.getLocalToParentTransform().deltaTransform(pos.multiply(zoomFactor - 1d));
    var newInnerBounds = zoomGroup.getBoundsInLocal();
    setHvalue((x + scrollPos.getX()) / (newInnerBounds.getWidth() - viewportBounds.getWidth()));
    setVvalue((y + scrollPos.getY()) / (newInnerBounds.getHeight() - viewportBounds.getHeight()));
  }
}
