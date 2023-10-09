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

import org.tybaco.editors.icon.Icons;
import org.tybaco.ui.model.Block;
import org.tybaco.ui.model.Link;

public final class DiagramBlock extends AbstractDiagramBlock {

  public final ProjectDiagram diagram;

  public DiagramBlock(ProjectDiagram diagram, Block block) {
    super(block);
    this.diagram = diagram;
    onClasspathChange();
  }

  public void onClasspathChange() {
    inputs.getChildren().clear();
    outputs.getChildren().clear();
    diagram.blockCache.blockById(block.factoryId).ifPresent(b -> {
      factory.setGraphic(Icons.icon(diagram.classpath.getClassLoader(), b.icon(), 32));
      b.forEachInput((spot, i) -> inputs.getChildren().add(new DiagramBlockInput(this, i, spot)));
      b.forEachOutput((spot, o) -> outputs.getChildren().add(new DiagramBlockOutput(this, o, spot)));
    });
  }

  public void onLink(Link link, boolean added) {
    for (var node : inputs.getChildren()) {
      if (node instanceof DiagramBlockInput i && link.inputMatches(block, i.spot)) {
        i.onLink(link, added);
      }
    }
    for (var node : outputs.getChildren()) {
      if (node instanceof DiagramBlockOutput o && link.outputMatches(block, o.spot)) {
        o.onLink(link, added);
      }
    }
  }
}
