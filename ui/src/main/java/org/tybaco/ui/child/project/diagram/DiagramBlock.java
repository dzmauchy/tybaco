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
import org.tybaco.editors.icon.Icons;
import org.tybaco.ui.child.project.classpath.BlockCache;
import org.tybaco.ui.model.Block;

public final class DiagramBlock extends AbstractDiagramBlock {

  final ProjectDiagram diagram;
  private final InvalidationListener listener = this::update;

  public DiagramBlock(ProjectDiagram diagram, Block block, BlockCache cache) {
    super(block);
    this.diagram = diagram;
    update(cache);
    initialize(cache);
  }

  private void initialize(BlockCache cache) {
    cache.addListener(new WeakInvalidationListener(listener));
  }

  private void update(Observable observable) {
    var cache = (BlockCache) observable;
    inputs.getChildren().clear();
    outputs.getChildren().clear();
    cache.blockById(block.factoryId).ifPresent(b -> {
      factory.setGraphic(Icons.icon(b.icon(), 32));
      b.inputs().forEach((name, i) -> {
        var input = new DiagramBlockInput(this, i, name);
        inputs.getChildren().add(input);
      });
      b.outputs().forEach((name, o) -> {
        var output = new DiagramBlockOutput(this, o, name);
        outputs.getChildren().add(output);
      });
    });
  }
}
