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

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.control.ToggleButton;
import org.tybaco.editors.icon.Icons;
import org.tybaco.editors.model.LibOutput;
import org.tybaco.ui.model.Connector;
import org.tybaco.ui.model.Link;

import static javafx.collections.FXCollections.observableHashMap;
import static org.tybaco.ui.child.project.diagram.DiagramCalculations.boundsIn;

public final class DiagramBlockOutput extends ToggleButton {

  private final ChangeListener<Boolean> separatedChangeListener = this::onSeparatedChange;
  private final WeakChangeListener<Boolean> weakSeparatedChangeListener = new WeakChangeListener<>(separatedChangeListener);
  final SimpleMapProperty<Link, Boolean> links = new SimpleMapProperty<>(this, "links", observableHashMap());
  final DiagramBlock block;
  final LibOutput output;
  final String spot;
  final DiagramBlockOutputCompanion companion;

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
    companion = new DiagramBlockOutputCompanion(this);
    links.addListener((Observable o) -> {
      System.out.println(links);
    });
  }

  private ClassLoader classLoader() {
    return block.diagram.classpath.getClassLoader();
  }

  void onLink(Link link, boolean added) {
    if (added) {
      links.put(link, link.separated.get());
      link.separated.addListener(weakSeparatedChangeListener);
      link.output.set(this);
    } else {
      links.remove(link);
      if (link.output.get() == this) link.output.set(null);
    }
  }

  private void onSeparatedChange(Observable observable, Boolean oldValue, Boolean newValue) {
    if (observable instanceof BooleanProperty p && p.getBean() instanceof Link l) {
      links.put(l, newValue);
    }
  }

  Bounds spotBounds() {
    return companion.isVisible() ? boundsIn(block.diagram.connectors, companion) : boundsIn(block.diagram.blocks, this);
  }

  @Override
  public String toString() {
    return new Connector(block.block.id, spot).toString();
  }
}
