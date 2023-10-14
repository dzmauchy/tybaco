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

import javafx.collections.SetChangeListener;
import javafx.collections.WeakSetChangeListener;
import org.tybaco.editors.icon.Icons;
import org.tybaco.ui.model.Block;
import org.tybaco.ui.model.Link;

import static java.util.Collections.binarySearch;

public final class DiagramBlock extends AbstractDiagramBlock {

  final Diagram diagram;
  final SetChangeListener<Link> linksListener = c -> onLink(c.wasAdded() ? c.getElementAdded() : c.getElementRemoved(), c.wasAdded());

  public DiagramBlock(Diagram diagram, Block block) {
    super(block);
    this.diagram = diagram;
    this.diagram.project.links.addListener(new WeakSetChangeListener<>(linksListener));
    this.diagram.blockCache.blockById(block.factoryId).ifPresent(b -> {
      factory.setGraphic(Icons.icon(diagram.classpath.getClassLoader(), b.icon(), 32));
      b.forEachInput((spot, i) -> inputs.getChildren().add(new DiagramBlockInput(this, i, spot, -1)));
      b.forEachOutput((spot, o) -> outputs.getChildren().add(new DiagramBlockOutput(this, o, spot)));
    });
    this.diagram.project.links.forEach(l -> onLink(l, true));
  }

  private void onLink(Link e, boolean added) {
    if (e.in.blockId != block.id && e.out.blockId != block.id) return;
    if (e.index >= 0 && e.in.blockId == block.id) {
      if (added) {
        var baseIndex = binarySearch(inputs.getChildren(), new Link(e.out, e.in, -1), DiagramBlockInput::cmp);
        var index = binarySearch(inputs.getChildren(), e, DiagramBlockInput::cmp);
        if (index < 0 && baseIndex >= 0 && inputs.getChildren().get(baseIndex) instanceof DiagramBlockInput i) {
          inputs.getChildren().add(-(index + 1), new DiagramBlockInput(this, i.input, i.spot, e.index));
        }
      } else {
        inputs.getChildren().removeIf(v -> v instanceof DiagramBlockInput i && i.spot.equals(e.in.spot) && i.index == e.index);
      }
    }
    if (e.in.blockId == block.id) {
      var index = binarySearch(inputs.getChildren(), e, DiagramBlockInput::cmp);
      if (index >= 0 && inputs.getChildren().get(index) instanceof DiagramBlockInput i) {
        i.onLink(e, added);
      }
    }
    if (e.out.blockId == block.id) {
      for (var node : outputs.getChildren()) {
        if (node instanceof DiagramBlockOutput o && o.block.block.id == e.out.blockId && o.spot.equals(e.out.spot)) {
          o.onLink(e, added);
          break;
        }
      }
    }
  }
}
