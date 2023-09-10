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
    factory.setText(block.factory);
    value.textProperty().bind(block.value);
    layoutXProperty().bind(block.x);
    layoutYProperty().bind(block.y);
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
    block.x.set(bx + getLayoutX() + event.getX());
    block.y.set(by + getLayoutY() + event.getY());
  }
}
