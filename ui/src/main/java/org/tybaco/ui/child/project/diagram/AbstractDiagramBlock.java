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

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tybaco.ui.model.Block;

import static javafx.scene.layout.BorderStrokeStyle.SOLID;
import static javafx.scene.paint.Color.WHITE;

abstract class AbstractDiagramBlock extends BorderPane {

  protected final Block block;
  protected final Label title = new Label();
  protected final Label factory = new Label();
  protected final VBox inputs = new VBox(2);
  protected final VBox outputs = new VBox(2);

  protected double bx;
  protected double by;

  AbstractDiagramBlock(Block block) {
    this.block = block;
    factory.setFocusTraversable(false);
    title.textProperty().bind(block.name);
    setLayoutX(block.x.get());
    setLayoutY(block.y.get());
    block.x.bind(layoutXProperty());
    block.y.bind(layoutYProperty());
    setTop(title);
    setCenter(factory);
    setLeft(inputs);
    setRight(outputs);
    inputs.setId("inputs");
    inputs.setAlignment(Pos.CENTER);
    outputs.setAlignment(Pos.CENTER);
    outputs.setId("outputs");
    inputs.setFillWidth(true);
    outputs.setFillWidth(true);
    setBorder(new Border(new BorderStroke(WHITE, SOLID, new CornerRadii(5d), new BorderWidths(2d))));
    title.setBorder(new Border(new BorderStroke(WHITE, SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 2, 0))));
    title.setPadding(new Insets(5d));
    title.setAlignment(Pos.CENTER);
    title.setStyle("-fx-background-color: linear-gradient(to top, black, transparent);");
    title.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 12));
    title.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    addEventHandler(MouseEvent.MOUSE_MOVED, this::onMouseMoved);
    addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
  }

  protected void onMouseMoved(MouseEvent event) {
    bx = event.getX();
    by = event.getY();
  }

  protected void onMouseDragged(MouseEvent event) {
    event.consume();
    setLayoutX(getLayoutX() + event.getX() - bx);
    setLayoutY(getLayoutY() + event.getY() - by);
  }
}
