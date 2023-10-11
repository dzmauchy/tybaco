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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.SetChangeListener;
import javafx.collections.WeakSetChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import org.tybaco.editors.icon.Icons;
import org.tybaco.editors.model.LibOutput;
import org.tybaco.ui.model.Link;

import java.util.function.Consumer;

public final class DiagramBlockOutput extends ToggleButton {

  public final DiagramBlock block;
  public final LibOutput output;
  public final String spot;
  private final InvalidationListener spotPointListener = this::onSpotPointUpdate;
  private final SetChangeListener<Link> linksListener = this::onLinksChange;

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
    sceneProperty().addListener(new ChangeListener<>() {
      @Override
      public void changed(ObservableValue<? extends Scene> observable, Scene oldValue, Scene newValue) {
        sceneProperty().removeListener(this);
        var wsl = new WeakInvalidationListener(spotPointListener);
        block.diagram.project.links.addListener(new WeakSetChangeListener<>(linksListener));
        onSpotPointUpdate(null);
      }
    });
  }

  private ClassLoader classLoader() {
    return block.diagram.classpath.getClassLoader();
  }

  private void onSpotPointUpdate(Observable observable) {
    var p = spotPoint();
    block.diagram.project.links.forEach(l -> {
      if (l.out.blockId == block.block.id) l.setOutPoint(p);
    });
  }

  private Point2D spotPoint() {
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
    return new Point2D(x, y);
  }

  private void onLinksChange(SetChangeListener.Change<? extends Link> change) {
    if (change.wasAdded()) {
      var l = change.getElementAdded();
      if (l.out.blockId == block.block.id) l.setOutPoint(spotPoint());
    }
  }
}
