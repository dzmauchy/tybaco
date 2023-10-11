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
import javafx.beans.Observable;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import org.tybaco.editors.icon.Icons;
import org.tybaco.editors.model.LibOutput;

public final class DiagramBlockOutput extends ToggleButton {

  public final DiagramBlock block;
  public final LibOutput output;
  public final String spot;
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
    visibleProperty().addListener(new InvalidationListener() {
      @Override
      public void invalidated(Observable observable) {
        visibleProperty().removeListener(this);
        block.boundsInParentProperty().addListener(boundsListener);
      }
    });
  }

  private ClassLoader classLoader() {
    return block.diagram.classpath.getClassLoader();
  }

  private void onBoundsUpdate(Observable observable) {
    System.out.println(System.nanoTime());
    var base = block.diagram.blocks;
    var bounds = getBoundsInLocal();
    var x = bounds.getMaxX();
    var y = bounds.getCenterY();
    for (Node c = this; c != base; c = c.getParent()) {
      var t = c.getLocalToParentTransform();
      var p = t.transform(x, y);
      x = p.getX();
      y = p.getY();
    }
  }
}
