package org.tybaco.ui.child.project.diagram;

import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import org.tybaco.ui.model.Block;

public class DiagramBlock extends BorderPane {

  final Block block;
  private final Label title = new Label();
  private final Label factory = new Label();
  private final Label value = new Label();
  private final VBox args = new VBox();
  private final VBox outputs = new VBox();
  private final HBox inputs = new HBox();
  private final BorderPane content = new BorderPane(value, factory, outputs, inputs, args);

  private double bx;
  private double by;

  public DiagramBlock(Block block) {
    this.block = block;
    title.textProperty().bind(block.name);
    factory.textProperty().bind(block.factory);
    value.textProperty().bind(block.value);
    init();
  }

  private void init() {
    setTop(title);
    setCenter(content);
    getStyleClass().add("diagram-block");
    title.getStyleClass().add("ty-title");
    value.getStyleClass().add("ty-block-value");
    factory.getStyleClass().add("ty-block-factory");
    content.getStyleClass().add("ty-content");
    outputs.getStyleClass().add("ty-outputs");
    inputs.getStyleClass().add("ty-inputs");
    args.getStyleClass().add("ty-args");
    title.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    factory.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    value.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    addEventHandler(MouseEvent.MOUSE_MOVED, this::onMouseMoved);
    addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
  }

  private void onMouseMoved(MouseEvent event) {
    bx = -event.getX();
    by = -event.getY();
  }

  private void onMouseDragged(MouseEvent event) {
    event.consume();
    setLayoutX(bx + getLayoutX() + event.getX());
    setLayoutY(by + getLayoutY() + event.getY());
    block.pos.set(new Point2D(getLayoutX(), getLayoutY()));
  }
}
