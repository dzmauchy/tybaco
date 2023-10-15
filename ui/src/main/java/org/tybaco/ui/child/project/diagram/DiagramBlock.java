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

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import org.tybaco.editors.icon.Icons;
import org.tybaco.ui.model.Block;
import org.tybaco.ui.model.Link;

import java.util.TreeMap;

public final class DiagramBlock extends AbstractDiagramBlock {

  final Diagram diagram;
  final TreeMap<String, TreeMap<Integer, DiagramBlockInput>> inputMap = new TreeMap<>();

  public DiagramBlock(Diagram diagram, Block block) {
    super(block);
    this.diagram = diagram;
    this.inputs.getChildren().addListener((ListChangeListener<? super Node>) c -> {
      while (c.next()) {
        if (c.wasRemoved()) {
          c.getRemoved().forEach(n -> {
            if (n instanceof DiagramBlockInput i) {
              inputMap.compute(i.spot, (s, o) -> {
                if (o != null) {
                  o.remove(i.index);
                  if (o.isEmpty()) return null;
                }
                return o;
              });
            }
          });
        }
        if (c.wasAdded()) {
          c.getAddedSubList().forEach(n -> {
            if (n instanceof DiagramBlockInput i) {
              inputMap.computeIfAbsent(i.spot, s -> new TreeMap<>()).put(i.index, i);
            }
          });
        }
      }
    });
    this.diagram.blockCache.blockById(block.factoryId).ifPresent(b -> {
      factory.setGraphic(Icons.icon(diagram.classpath.getClassLoader(), b.icon(), 32));
      b.forEachInput((spot, i) -> inputs.getChildren().add(new DiagramBlockInput(this, i, spot, -1)));
      b.forEachOutput((spot, o) -> outputs.getChildren().add(new DiagramBlockOutput(this, o, spot)));
    });
  }

  void onLink(Link e, boolean added) {
    if (e.index >= 0 && e.in.blockId == block.id) {
      if (added) {
        var m = inputMap.get(e.in.spot);
        if (m != null && !m.containsKey(e.index)) {
          var entry = m.floorEntry(e.index);
          if (entry == null) {
            var input = m.get(-1);
            var index = inputs.getChildren().indexOf(input);
            inputs.getChildren().add(index + 1, new DiagramBlockInput(this, input.input, e.in.spot, e.index));
          } else {
            var index = inputs.getChildren().indexOf(entry.getValue());
            inputs.getChildren().add(index + 1, new DiagramBlockInput(this, entry.getValue().input, e.in.spot, e.index));
          }
        }
      } else {
        inputs.getChildren().removeIf(v -> v instanceof DiagramBlockInput i && i.spot.equals(e.in.spot) && i.index == e.index);
      }
    }
    if (e.in.blockId == block.id) {
      var m = inputMap.get(e.in.spot);
      if (m != null) {
        var input = m.get(e.index);
        if (input != null) {
          input.onLink(e, added);
        }
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
