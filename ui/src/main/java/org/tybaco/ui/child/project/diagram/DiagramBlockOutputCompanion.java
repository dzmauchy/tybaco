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
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.TextAlignment;

import static javafx.beans.binding.Bindings.createDoubleBinding;
import static javafx.scene.layout.BorderStrokeStyle.SOLID;
import static javafx.scene.paint.Color.WHITE;
import static org.tybaco.ui.child.project.diagram.DiagramCalculations.spotPointBinding;

public final class DiagramBlockOutputCompanion extends Group {

  public final DiagramBlockOutput output;
  private final Label label = new Label();
  private final Line line = new Line();

  DiagramBlockOutputCompanion(DiagramBlockOutput output) {
    this.output = output;
    label.setText(output.block.block.id + "." + output.spot);
    label.setAlignment(Pos.CENTER);
    label.setTextAlignment(TextAlignment.CENTER);
    label.setBorder(new Border(new BorderStroke(WHITE, SOLID, new CornerRadii(4d), new BorderWidths(1d))));
    label.setTextFill(WHITE);
    label.setPadding(new Insets(0, 3d, 0, 3d));
    line.setStroke(WHITE);
    line.setStrokeWidth(2d);
    getChildren().addAll(line, label);
    output.sceneProperty().addListener((o, os, ns) -> {
      if (ns != null) {
        var lp = spotPointBinding(output.block.diagram.blocks, output, b -> new Point2D(b.getMaxX(), b.getMinY()));
        label.layoutXProperty().bind(createDoubleBinding(() -> lp.get().getX() + 10d, lp));
        label.layoutYProperty().bind(createDoubleBinding(() -> lp.get().getY(), lp));
        label.prefHeightProperty().bind(output.heightProperty());
        line.startXProperty().bind(createDoubleBinding(() -> lp.get().getX(), lp));
        line.startYProperty().bind(createDoubleBinding(() -> lp.get().getY() + label.getHeight() / 2d, lp, label.heightProperty()));
        line.endXProperty().bind(createDoubleBinding(() -> line.getStartX() + 10d, line.startXProperty()));
        line.endYProperty().bind(createDoubleBinding(line::getStartY, line.startYProperty()));
        output.block.diagram.connectors.getChildren().add(this);
      } else {
        output.block.diagram.connectors.getChildren().remove(this);
      }
    });
  }
}
