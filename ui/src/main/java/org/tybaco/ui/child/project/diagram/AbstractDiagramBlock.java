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

import javafx.geometry.*;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tybaco.ui.model.Block;

import static javafx.geometry.Orientation.VERTICAL;
import static javafx.scene.layout.BorderStrokeStyle.SOLID;
import static javafx.scene.paint.Color.WHITE;

abstract class AbstractDiagramBlock extends BorderPane {

  final Diagram diagram;
  final Block block;
  private final Label blockIdLabel = new Label();
  final Label blockName = new Label();
  final HBox title = new HBox(3d, blockIdLabel, new Separator(VERTICAL), blockName);
  final Label factory = new Label();
  final VBox inputs = new VBox(3);
  final VBox outputs = new VBox(3);

  double bx;
  double by;

  AbstractDiagramBlock(Diagram diagram, Block block) {
    this.diagram = diagram;
    this.block = block;
    setBackground(new Background(new BackgroundFill(Color.gray(0.2), new CornerRadii(5), Insets.EMPTY)));
    setBorder(new Border(new BorderStroke(WHITE, SOLID, new CornerRadii(5d), new BorderWidths(2d))));
    setLayoutX(block.x.get());
    setLayoutY(block.y.get());
    block.x.bind(layoutXProperty());
    block.y.bind(layoutYProperty());
    configureFactoryAndBlockId();
    configureInputsAndOutputs();
    configureBlockName();
    configureTitle();
    addEventHandler(MouseEvent.MOUSE_MOVED, this::onMouseMoved);
    addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
  }

  private void configureTitle() {
    setTop(title);
    title.setBorder(new Border(new BorderStroke(WHITE, SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 2, 0))));
    title.setPadding(new Insets(5d));
    title.setFillHeight(true);
    title.setStyle("-fx-background-color: linear-gradient(to top, black, transparent);");
  }

  private void configureBlockName() {
    blockName.textProperty().bind(block.name);
    blockName.setFont(Font.font(null, FontWeight.BOLD, 12));
    blockName.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    blockName.setAlignment(Pos.BASELINE_CENTER);
    HBox.setHgrow(blockName, Priority.ALWAYS);
  }

  private void configureInputsAndOutputs() {
    setLeft(inputs);
    setRight(outputs);
    inputs.setAlignment(Pos.CENTER);
    outputs.setAlignment(Pos.CENTER);
    inputs.setFillWidth(true);
    outputs.setFillWidth(true);
  }

  private void configureFactoryAndBlockId() {
    factory.setPadding(new Insets(4d));
    setCenter(factory);
    blockIdLabel.setText(Integer.toString(block.id));
    blockIdLabel.setAlignment(Pos.BASELINE_CENTER);
  }

  protected void onMouseMoved(MouseEvent event) {
    bx = event.getX();
    by = event.getY();
  }

  protected void onMouseDragged(MouseEvent event) {
    event.consume();
    var bounds = getBoundsInParent();
    var bb = new BoundingBox(
      bounds.getMinX() + event.getX() - bx,
      bounds.getMinY() + event.getY() - by,
      bounds.getWidth(),
      bounds.getHeight()
    );
    checkConstraints(bb);
    setLayoutX(getLayoutX() + event.getX() - bx);
    setLayoutY(getLayoutY() + event.getY() - by);
  }

  private void checkConstraints(Bounds bounds) {
    for (var node : diagram.blocks.getChildren()) {
      if (node == this) continue;
      var b = node.getBoundsInParent();
      var bb = new BoundingBox(
        b.getMinX() - 35d,
        b.getMinY() - 35d,
        b.getWidth() + 70d,
        b.getHeight() + 70d
      );
      if (bb.intersects(bounds)) {
      }
    }
  }
}
