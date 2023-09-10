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

abstract class AbstractDiagramBlock extends BorderPane {

  protected final Label title = new Label();
  protected final Label factory = new Label();
  protected final Label value = new Label();
  protected final VBox args = new VBox();
  protected final VBox outputs = new VBox();
  protected final HBox inputs = new HBox();
  protected final BorderPane content = new BorderPane(value, factory, outputs, inputs, args);

  protected double bx;
  protected double by;

  AbstractDiagramBlock() {
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

  protected void onMouseMoved(MouseEvent event) {
    bx = -event.getX();
    by = -event.getY();
  }

  protected void onMouseDragged(MouseEvent event) {
    event.consume();
    setLayoutX(bx + getLayoutX() + event.getX());
    setLayoutY(by + getLayoutY() + event.getY());
  }
}
