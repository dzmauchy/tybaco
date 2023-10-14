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
import javafx.collections.SetChangeListener;
import javafx.scene.control.ToggleButton;
import org.tybaco.editors.icon.Icons;
import org.tybaco.editors.model.LibOutput;
import org.tybaco.ui.model.Connector;
import org.tybaco.ui.model.Link;

import static org.tybaco.editors.base.ObservableSets.filteredSet;

public final class DiagramBlockOutput extends ToggleButton {

  final DiagramBlock block;
  final LibOutput output;
  final String spot;
  final SimpleSetProperty<Link> links = new SimpleSetProperty<>(this, "links");
  final DiagramBlockOutputCompanion companion;

  public DiagramBlockOutput(DiagramBlock block, LibOutput output, String spot) {
    setFocusTraversable(false);
    this.block = block;
    this.output = output;
    this.spot = spot;
    this.companion = new DiagramBlockOutputCompanion(this);
    this.links.set(filteredSet(block.links, l -> l.out.blockId == block.block.id && spot.equals(l.out.spot)));
    this.links.addListener((SetChangeListener<? super Link>) c -> {
      if (c.wasRemoved()) {
        if (c.getElementRemoved().output.get() == this) {
          c.getElementRemoved().output.set(null);
        }
      } else if (c.wasAdded()) {
        c.getElementAdded().output.set(this);
      }
    });
    setToggleGroup(block.diagram.outputToggleGroup);
    setGraphic(Icons.icon(classLoader(), output.icon(), 20));
    setTooltip(DiagramTooltips.tooltip(classLoader(), output));
    selectedProperty().addListener((o, ov, nv) -> {
      if (nv) block.diagram.currentOutput = this;
    });
  }

  private ClassLoader classLoader() {
    return block.diagram.classpath.getClassLoader();
  }

  @Override
  public String toString() {
    return new Connector(block.block.id, spot).toString();
  }
}
