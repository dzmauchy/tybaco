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
import org.tybaco.editors.model.LibInput;
import org.tybaco.ui.model.Connector;
import org.tybaco.ui.model.Link;

import static java.util.Collections.binarySearch;
import static java.util.Comparator.comparingInt;
import static org.tybaco.editors.icon.Icons.icon;
import static org.tybaco.ui.child.project.diagram.DiagramSpotPoints.installSpotPointMonitoring;

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
    vectorInputs.setFillWidth(true);
  }

  private ClassLoader classLoader() {
    return block.diagram.classpath.getClassLoader();
  }

  public void onLink(Link link, boolean added) {
    if (added) {
      inputButton.setUnderline(true);
      if (link.index >= 0) {
        var b = new Button(Integer.toString(link.index));
        b.setFocusTraversable(false);
        b.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        b.setUserData(link);
        b.setOnAction(this::onVectorButton);
        var i = binarySearch(vectorInputs.getChildren(), b, comparingInt(n -> ((Link) n.getUserData()).index));
        if (i < 0) {
          vectorInputs.getChildren().add(-(i + 1), b);
          installSpotPointMonitoring(block.diagram.blocks, b, DiagramSpotPoints::inputSpot, link.inpSpot::bind);
        }
      } else {
        installSpotPointMonitoring(block.diagram.blocks, inputButton, DiagramSpotPoints::inputSpot, link.inpSpot::bind);
      }
    } else {
      if (link.index < 0) {
        inputButton.setUnderline(false);
        inputButton.setUserData(null);
      } else {
        vectorInputs.getChildren().removeIf(n -> link.equals(n.getUserData()));
        if (vectorInputs.getChildren().isEmpty()) {
          inputButton.setUnderline(false);
        }
      }
    }
  }

  private void onButton(ActionEvent event) {
    var co = block.diagram.currentOutput;
    if (co == null) {

    } else {
      co.setSelected(false);
      var out = new Connector(co.block.block.id, co.spot);
      if (input.vector()) {
        var i = vectorInputs.getChildren().stream().mapToInt(n -> ((Link) n.getUserData()).index).max().orElse(-1) + 1;
        block.diagram.project.links.add(new Link(out, inp, i));
      } else {
        block.diagram.project.links.removeIf(l -> l.in.equals(inp));
        block.diagram.project.links.add(new Link(out, inp));
      }
      block.diagram.currentOutput = null;
    }
  }

  private void onVectorButton(ActionEvent event) {
    var co = block.diagram.currentOutput;
    if (co == null) {

    } else {
      co.setSelected(false);
      var button = (Button) event.getSource();
      var out = new Connector(co.block.block.id, co.spot);
      var link = (Link) button.getUserData();
      block.diagram.project.links.removeIf(l -> l.equals(link));
      block.diagram.project.links.add(new Link(out, inp, link.index));
      block.diagram.currentOutput = null;
    }
  }
}
