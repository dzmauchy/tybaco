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

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.tybaco.editors.icon.Icons;
import org.tybaco.editors.model.LibInput;
import org.tybaco.ui.model.Connector;
import org.tybaco.ui.model.Link;

import static java.util.Collections.binarySearch;
import static java.util.Comparator.comparing;
import static org.tybaco.editors.icon.Icons.icon;

public final class DiagramBlockInput extends BorderPane {

  public final DiagramBlock block;
  public final LibInput input;
  public final String spot;
  public final Connector inp;
  private final VBox vectorInputs;
  private final Button inputButton;

  public DiagramBlockInput(DiagramBlock block, LibInput input, String spot) {
    this.block = block;
    this.input = input;
    this.spot = spot;
    this.inp = new Connector(block.block.id, spot);
    setTop(inputButton = new Button(null, icon(classLoader(), input.icon(), 20)));
    inputButton.setFocusTraversable(false);
    inputButton.setTooltip(DiagramTooltips.tooltip(classLoader(), input));
    inputButton.setOnAction(this::onButton);
    setCenter(vectorInputs = new VBox());
  }

  private ClassLoader classLoader() {
    return block.diagram.classpath.getClassLoader();
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

  private void onButton(ActionEvent event) {
    if (block.diagram.currentOutput == null) {

    } else {
      block.diagram.currentOutput.setSelected(false);
      var out = new Connector(block.diagram.currentOutput.block.block.id, block.diagram.currentOutput.spot);
      if (input.vector()) {
        var i = vectorInputs.getChildren().stream().mapToInt(n -> (int) n.getUserData()).max().orElse(-1) + 1;
        block.diagram.project.links.add(new Link(out, inp, i));
      } else {
        block.diagram.project.links.removeIf(l -> l.in.equals(inp));
        block.diagram.project.links.add(new Link(out, inp));
      }
      block.diagram.currentOutput = null;
    }
  }

  private void onVectorButton(ActionEvent event) {
    if (block.diagram.currentOutput == null) {

    } else {
      block.diagram.currentOutput.setSelected(false);
      var button = (Button) event.getSource();
      var out = new Connector(block.diagram.currentOutput.block.block.id, block.diagram.currentOutput.spot);
      var i = (int) button.getUserData();
      block.diagram.project.links.removeIf(l -> l.in.equals(inp) && l.index == i);
      block.diagram.project.links.add(new Link(out, inp, i));
      block.diagram.currentOutput = null;
    }
  }
}
