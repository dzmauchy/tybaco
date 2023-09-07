package org.tybaco.ui.child.project.diagram;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.tybaco.ui.model.Block;

public class DiagramBlock extends BorderPane {

  private final Block block;
  private final Label title = new Label();

  public DiagramBlock(Block block) {
    this.block = block;
    getStyleClass().add("diagram-block");
    setTop(title);
    title.getStyleClass().add("title");
    title.textProperty().bind(block.name);
  }
}
