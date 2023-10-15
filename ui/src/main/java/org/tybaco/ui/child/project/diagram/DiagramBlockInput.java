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
import org.tybaco.editors.base.ObservableBounds;
import org.tybaco.editors.icon.Icons;
import org.tybaco.editors.model.LibInput;
import org.tybaco.ui.model.Connector;
import org.tybaco.ui.model.Link;

public final class DiagramBlockInput extends Button {

  final DiagramBlock block;
  final LibInput input;
  final String spot;
  final int index;
  final Connector inp;
  final ObservableBounds spotBounds;

  private Link link;

  public DiagramBlockInput(DiagramBlock block, LibInput input, String spot, int index) {
    this.block = block;
    this.input = input;
    this.spot = spot;
    this.index = index;
    this.inp = new Connector(block.block.id, spot);
    this.spotBounds = new ObservableBounds(block.diagram.blocks, this);
    this.spotBounds.addListener((o, ov, nv) -> {
      if (link != null) link.inBounds.set(nv);
    });
    if (index < 0) {
      setGraphic(Icons.icon(classLoader(), input.icon(), 20));
    } else {
      setText(Integer.toString(index));
    }
    setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    setTooltip(DiagramTooltips.tooltip(classLoader(), input));
    setFocusTraversable(false);
    setOnAction(this::onButton);
  }

  void onLink(Link link, boolean added) {
    if (added) {
      this.link = link;
      link.input.set(this);
      link.inBounds.set(spotBounds.getValue());
    } else {
      this.link = null;
      link.input.set(null);
    }
  }

  private ClassLoader classLoader() {
    return block.diagram.classpath.getClassLoader();
  }

  private void onButton(ActionEvent event) {
    var co = block.diagram.currentOutput;
    if (co == null) {

    } else {
      co.setSelected(false);
      var out = new Connector(co.block.block.id, co.spot);
      if (input.vector() && index < 0) {
        var m = block.inputMap.get(spot);
        var nextIndex = m.lastKey();
        var entry = m.floorEntry(nextIndex);
        var insertionIndex = block.inputs.getChildren().indexOf(entry.getValue()) + 1;
        var b = new DiagramBlockInput(block, input, spot, nextIndex);
        block.inputs.getChildren().add(insertionIndex, b);
        block.diagram.project.links.add(new Link(out, inp, nextIndex));
      } else {
        block.diagram.project.links.removeIf(l -> l.in.equals(inp) && l.index == index);
        block.diagram.project.links.add(new Link(out, inp, index));
      }
      block.diagram.currentOutput = null;
    }
  }

  @Override
  public String toString() {
    return block.block.id + "." + spot + "[" + index + "]";
  }
}
