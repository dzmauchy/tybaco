package org.tybloco.ui.child.project.diagram;

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
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tybloco.editors.Meta;
import org.tybloco.editors.icon.Icons;
import org.tybloco.editors.text.Texts;

import static javafx.geometry.Orientation.HORIZONTAL;

interface DiagramTooltips {

  static Tooltip tooltip(ClassLoader classLoader, Meta meta) {
    var tooltip = new Tooltip();
    tooltip.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    tooltip.setMaxSize(PopupControl.USE_PREF_SIZE, 500);
    var label = new Label();
    label.setGraphic(Icons.icon(classLoader, meta.icon(), 64));
    label.textProperty().bind(Texts.text(classLoader, meta.name()));
    label.setFont(Font.font(null, FontWeight.BOLD, 18d));
    label.setGraphicTextGap(8d);
    var description = new Label();
    description.setMinWidth(600d);
    description.setPadding(new Insets(5d));
    description.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    description.setWrapText(true);
    description.setStyle("-fx-background-color: linear-gradient(to bottom, black, transparent);");
    description.setFont(Font.font(null, 14));
    description.textProperty().bind(Texts.text(classLoader, meta.description()));
    description.setAlignment(Pos.TOP_LEFT);
    var box = new VBox(5, label, new Separator(HORIZONTAL), description);
    VBox.setVgrow(description, Priority.ALWAYS);
    tooltip.setGraphic(box);
    return tooltip;
  }
}
