package org.tybaco.ui.child.project.diagram;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.tybaco.ui.model.Block;

public class DiagramBlock extends BorderPane {

  final Block block;
  private final Label title = new Label();
  private final Label factory = new Label();
  private final Label value = new Label();
  private final VBox content = new VBox(factory, value);

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
    content.getStyleClass().add("ty-content");
    title.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    factory.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    value.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
  }
}
