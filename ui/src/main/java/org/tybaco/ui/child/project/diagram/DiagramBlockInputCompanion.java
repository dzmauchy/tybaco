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
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;

import static javafx.beans.binding.Bindings.createDoubleBinding;
import static javafx.scene.layout.BorderStrokeStyle.SOLID;
import static javafx.scene.paint.Color.WHITE;
import static org.tybaco.ui.child.project.diagram.DiagramCalculations.boundsBinding;

public final class DiagramBlockInputCompanion extends Group {

  final DiagramBlockInput input;
  private final Label label = new Label();
  private final Line line = new Line();

  DiagramBlockInputCompanion(DiagramBlockInput input) {
    this.input = input;
    label.setAlignment(Pos.CENTER);
    label.setBorder(new Border(new BorderStroke(WHITE, SOLID, new CornerRadii(40, 0, 0, 40, true), new BorderWidths(1d))));
    label.setTextFill(WHITE);
    label.setPadding(new Insets(1d, 5d, 1d, 7d));
    line.setStroke(WHITE);
    line.setStrokeWidth(2d);
    getChildren().addAll(line, label);
    input.sceneProperty().addListener((o, os, ns) -> {
      if (ns != null) {
        var bb = boundsBinding(input.block.diagram.blocks, input);
        label.layoutXProperty().bind(createDoubleBinding(() -> bb.get().getMinX() - 10d - label.getWidth(), bb, label.widthProperty()));
        label.layoutYProperty().bind(createDoubleBinding(() -> bb.get().getMinY(), bb));
        label.prefHeightProperty().bind(input.heightProperty());
        line.startXProperty().bind(createDoubleBinding(() -> bb.get().getMinX() - 10d, bb));
        line.startYProperty().bind(createDoubleBinding(() -> bb.get().getCenterY(), bb));
        line.endXProperty().bind(createDoubleBinding(() -> bb.get().getMinX(), bb));
        line.endYProperty().bind(line.startYProperty());
        input.block.diagram.companions.getChildren().add(this);
      } else {
        input.block.diagram.companions.getChildren().remove(this);
      }
    });
    visibleProperty().bind(input.link.flatMap(l -> l.separated));
    label.textProperty().bind(input.link.map(l -> l.out.toString()));
  }
}
