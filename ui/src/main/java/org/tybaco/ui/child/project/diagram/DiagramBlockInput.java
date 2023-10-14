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

import javafx.beans.property.SimpleSetProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import org.tybaco.editors.icon.Icons;
import org.tybaco.editors.model.LibInput;
import org.tybaco.ui.model.Connector;
import org.tybaco.ui.model.Link;

import static java.util.Collections.binarySearch;
import static org.tybaco.editors.base.ObservableSets.filteredSet;

public final class DiagramBlockInput extends Button {

  final DiagramBlock block;
  final LibInput input;
  final String spot;
  final int index;
  final Connector inp;
  final SimpleSetProperty<Link> links = new SimpleSetProperty<>(this, "links");
  final ObservableValue<Link> link;
  final DiagramBlockInputCompanion companion;

  public DiagramBlockInput(DiagramBlock block, LibInput input, String spot, int index) {
    this.block = block;
    this.input = input;
    this.spot = spot;
    this.index = index;
    this.inp = new Connector(block.block.id, spot);
    this.links.set(filteredSet(block.links, l -> l.in.blockId == block.block.id && spot.equals(l.in.spot)));
    this.link = links.map(s -> s.stream().filter(l -> l.index == index).findFirst().orElse(null));
    this.link.addListener((o, ol, nl) -> {
      if (nl != null) {
        nl.input.set(this);
      } else if (ol != null && ol.input.get() == this) {
        ol.input.set(null);
      }
    });
    this.companion = new DiagramBlockInputCompanion(this);
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
        var nextIndex = block.inputs.getChildren().stream()
          .map(n -> (DiagramBlockInput) n)
          .filter(i -> spot.equals(i.spot))
          .mapToInt(i -> i.index)
          .max()
          .orElseThrow() + 1;
        var b = new DiagramBlockInput(block, input, spot, nextIndex);
        var i = binarySearch(block.inputs.getChildren(), b, DiagramBlockInput::cmp);
        block.inputs.getChildren().add(-(i + 1), b);
        block.diagram.project.links.add(new Link(out, inp, nextIndex));
      } else {
        block.diagram.project.links.removeIf(l -> l.in.equals(inp));
        block.diagram.project.links.add(new Link(out, inp, index));
      }
      block.diagram.currentOutput = null;
    }
  }

  static int cmp(Object o1, Object o2) {
    if (o1 instanceof DiagramBlockInput i1 && o2 instanceof DiagramBlockInput i2) {
      var c = i1.spot.compareTo(i2.spot);
      if (c != 0) return c;
      return Integer.compare(i1.index, i2.index);
    } else if (o1 instanceof DiagramBlockInput i1 && o2 instanceof Link l2) {
      var c = i1.spot.compareTo(l2.in.spot);
      if (c != 0) return c;
      return Integer.compare(i1.index, l2.index);
    } else {
      return 0;
    }
  }

  @Override
  public String toString() {
    return block.block.id + "." + spot + "[" + index + "]";
  }
}
