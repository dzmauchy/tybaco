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
import javafx.scene.Group;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;

import static javafx.beans.binding.Bindings.createBooleanBinding;
import static javafx.beans.binding.Bindings.createDoubleBinding;
import static javafx.scene.layout.BorderStrokeStyle.SOLID;
import static javafx.scene.paint.Color.WHITE;
import static org.tybaco.ui.child.project.diagram.DiagramCalculations.boundsBinding;

public final class DiagramBlockOutputCompanion extends Group {

  final DiagramBlockOutput output;
  private final Label label = new Label();
  private final Line line = new Line();

  DiagramBlockOutputCompanion(DiagramBlockOutput output) {
    this.output = output;
    label.setText(output.toString());
    label.setAlignment(Pos.CENTER);
    label.setContentDisplay(ContentDisplay.RIGHT);
    label.setBorder(new Border(new BorderStroke(WHITE, SOLID, new CornerRadii(0, 40, 40, 0, true), new BorderWidths(1d))));
    label.setTextFill(WHITE);
    label.setPadding(new Insets(1d, 7d, 1d, 5d));
    line.setStroke(WHITE);
    line.setStrokeWidth(2d);
    getChildren().addAll(line, label);
    output.sceneProperty().addListener((o, os, ns) -> {
      if (ns != null) {
        var bb = boundsBinding(output.block.diagram.blocks, output);
        label.layoutXProperty().bind(createDoubleBinding(() -> bb.get().getMaxX() + 10d, bb));
        label.layoutYProperty().bind(createDoubleBinding(() -> bb.get().getMinY(), bb));
        label.prefHeightProperty().bind(output.heightProperty());
        line.startXProperty().bind(createDoubleBinding(() -> bb.get().getMaxX(), bb));
        line.startYProperty().bind(createDoubleBinding(() -> bb.get().getCenterY(), bb));
        line.endXProperty().bind(createDoubleBinding(() -> bb.get().getMaxX() + 10d, bb));
        line.endYProperty().bind(line.startYProperty());
        output.block.diagram.connectors.getChildren().add(this);
      } else {
        output.block.diagram.connectors.getChildren().remove(this);
      }
    });
    visibleProperty().bind(createBooleanBinding(() -> output.links.keySet().stream().anyMatch(l -> l.separated.get()), output.links));
  }
}
