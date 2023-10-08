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
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.LinearGradient;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tybaco.editors.icon.Icons;
import org.tybaco.editors.model.LibInput;
import org.tybaco.editors.text.Texts;
import org.tybaco.ui.model.Link;

import static java.util.Collections.binarySearch;
import static java.util.Comparator.comparing;
import static javafx.geometry.Orientation.HORIZONTAL;

public final class DiagramBlockInput extends BorderPane {

  public final DiagramBlock block;
  public final LibInput input;
  public final String spot;
  private final VBox vectorInputs;
  private final Button inputButton;

  public DiagramBlockInput(DiagramBlock block, LibInput input, String spot) {
    this.block = block;
    this.input = input;
    this.spot = spot;
    setTop(inputButton = new Button(null, Icons.icon(classLoader(), input.icon(), 20)));
    inputButton.setFocusTraversable(false);
    inputButton.setTooltip(tooltip());
    setCenter(vectorInputs = new VBox());
  }

  private ClassLoader classLoader() {
    return block.diagram.classpath.getClassLoader();
  }

  private Tooltip tooltip() {
    var tooltip = new Tooltip();
    tooltip.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    tooltip.setMaxSize(USE_PREF_SIZE, 500);
    var label = new Label();
    label.setGraphic(Icons.icon(classLoader(), input.icon(), 64));
    label.textProperty().bind(Texts.text(classLoader(), input.name()));
    label.setFont(Font.font(null, FontWeight.BOLD, 18d));
    label.setGraphicTextGap(8d);
    var description = new Label();
    description.setMinWidth(600d);
    description.setPadding(new Insets(5d));
    description.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    description.setWrapText(true);
    description.setStyle("-fx-background-color: linear-gradient(to bottom, black, transparent);");
    description.setFont(Font.font(null, 14));
    description.textProperty().bind(Texts.text(classLoader(), input.description()));
    description.setAlignment(Pos.TOP_LEFT);
    var box = new VBox(5, label, new Separator(HORIZONTAL), description);
    VBox.setVgrow(description, Priority.ALWAYS);
    tooltip.setGraphic(box);
    return tooltip;
  }

  public void onLink(Link link, boolean added) {
    if (added) {
      inputButton.setUnderline(true);
      if (link.index >= 0) {
        var b = new Button(Integer.toString(link.index));
        b.setUserData(link.index);
        var i = binarySearch(vectorInputs.getChildren(), b, comparing(n -> (Integer) n.getUserData()));
        vectorInputs.getChildren().add(-(i + 1), b);
      }
    } else {
      if (link.index < 0) {
        inputButton.setUnderline(false);
      } else {
        var txtIndex = Integer.toString(link.index);
        vectorInputs.getChildren().removeIf(n -> n instanceof Button b && txtIndex.equals(b.getText()));
        if (vectorInputs.getChildren().isEmpty()) {
          inputButton.setUnderline(false);
        }
      }
    }
  }
}
