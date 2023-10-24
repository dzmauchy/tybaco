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

import javafx.scene.control.ToggleButton;
import org.tybloco.editors.base.ObservableBounds;
import org.tybloco.editors.icon.Icons;
import org.tybloco.editors.model.LibOutput;
import org.tybloco.ui.model.Connector;
import org.tybloco.ui.model.Link;

import java.util.HashMap;

public final class DiagramBlockOutput extends ToggleButton {

  public final DiagramBlock block;
  final LibOutput output;
  final String spot;
  final HashMap<Link, Boolean> links = new HashMap<>();
  private final ObservableBounds spotBounds;

  public DiagramBlockOutput(DiagramBlock block, LibOutput output, String spot) {
    setFocusTraversable(false);
    this.block = block;
    this.output = output;
    this.spot = spot;
    this.spotBounds = new ObservableBounds(block.diagram.blocks, this);
    this.spotBounds.addListener((o, ov, nv) -> links.forEach((l, b) -> l.outBounds.set(nv)));
    setToggleGroup(block.diagram.outputToggleGroup);
    setGraphic(Icons.icon(classLoader(), output.icon(), 20));
    setTooltip(DiagramTooltips.tooltip(classLoader(), output));
    selectedProperty().addListener((o, ov, nv) -> block.diagram.setCurrentOutput(nv, this));
  }

  void onLink(Link link, boolean added) {
    if (added) {
      links.put(link, Boolean.TRUE);
      link.output.set(this);
      link.outBounds.set(spotBounds.getValue());
    } else {
      links.remove(link);
      link.output.set(null);
    }
  }

  private ClassLoader classLoader() {
    return block.diagram.classpath.getClassLoader();
  }

  @Override
  public String toString() {
    return new Connector(block.block.id, spot).toString();
  }
}
