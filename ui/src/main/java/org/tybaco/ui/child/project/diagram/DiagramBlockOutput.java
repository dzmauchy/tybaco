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

import javafx.beans.InvalidationListener;
import javafx.geometry.Bounds;
import javafx.scene.control.ToggleButton;
import org.tybaco.editors.icon.Icons;
import org.tybaco.editors.model.LibOutput;
import org.tybaco.ui.model.Link;

import java.util.IdentityHashMap;

public final class DiagramBlockOutput extends ToggleButton {

  public final DiagramBlock block;
  public final LibOutput output;
  public final String spot;
  public final IdentityHashMap<Link, Boolean> links = new IdentityHashMap<>();

  public DiagramBlockOutput(DiagramBlock block, LibOutput output, String spot) {
    setFocusTraversable(false);
    this.block = block;
    this.output = output;
    this.spot = spot;
    setToggleGroup(block.diagram.outputToggleGroup);
    setGraphic(Icons.icon(classLoader(), output.icon(), 20));
    setTooltip(DiagramTooltips.tooltip(classLoader(), output));
    selectedProperty().addListener((o, ov, nv) -> {
      if (nv) block.diagram.currentOutput = this;
    });
    var layoutListener = (InvalidationListener) (o -> {
      var bounds = bounds();
      links.forEach((l, v) -> setupLink(l, bounds));
    });
    layoutBoundsProperty().addListener(layoutListener);
  }

  private ClassLoader classLoader() {
    return block.diagram.classpath.getClassLoader();
  }

  public void onLink(Link link, boolean added) {
    if (added) {
      links.put(link, Boolean.TRUE);
      setupLink(link, bounds());
    } else {
      links.remove(link);
    }
  }

  private Bounds bounds() {
    var boundsInBlock = getBoundsInParent();
    return block.localToParent(boundsInBlock);
  }

  private void setupLink(Link link, Bounds bounds) {
    link.outX.set(bounds.getMaxX());
    link.outY.set(bounds.getCenterY());
  }
}
