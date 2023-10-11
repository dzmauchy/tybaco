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

import javafx.beans.*;
import javafx.scene.Node;
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
  private final InvalidationListener boundsListener = this::onBoundsUpdate;

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
    initialize();
  }

  private void initialize() {
    boundsInParentProperty().addListener(boundsListener);
    block.outputs.boundsInParentProperty().addListener(new WeakInvalidationListener(boundsListener));
    block.boundsInParentProperty().addListener(new WeakInvalidationListener(boundsListener));
    onBoundsUpdate(boundsInParentProperty());
  }

  private ClassLoader classLoader() {
    return block.diagram.classpath.getClassLoader();
  }

  public void onLink(Link link, boolean added) {
    if (added) {
      links.put(link, Boolean.TRUE);
      onBoundsUpdate(boundsInParentProperty());
    } else {
      links.remove(link);
    }
  }

  private void onBoundsUpdate(Observable observable) {
    var base = block.diagram.blocks;
    var bounds = getBoundsInLocal();
    var x = bounds.getMinX();
    var y = bounds.getCenterY();
    for (Node c = this; c != base; c = c.getParent()) {
      if (c == null) {
        return;
      }
      var t = c.getLocalToParentTransform();
      var p = t.transform(x, y);
      x = p.getX();
      y = p.getY();
    }
    for (var l : links.keySet()) {
      l.outX.set(x);
      l.outY.set(y);
    }
  }
}
