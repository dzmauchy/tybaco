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

import javafx.collections.ListChangeListener.Change;
import org.springframework.stereotype.Component;
import org.tybaco.ui.child.project.classpath.BlockCache;
import org.tybaco.ui.model.Block;
import org.tybaco.ui.model.Project;

@Component
public class ProjectDiagram extends AbstractProjectDiagram {

  final Project project;

  public ProjectDiagram(Project project, BlockCache blockCache) {
    this.project = project;
    project.blocks.addListener((Change<? extends Block> c) -> {
      while (c.next()) {
        if (c.wasRemoved()) {
          for (var removed : c.getRemoved()) {
            for (var i = blocks.getChildren().iterator(); i.hasNext(); ) {
              var e = i.next();
              if (e instanceof DiagramBlock db && db.block == removed) {
                i.remove();
                break;
              }
            }
          }
        } else if (c.wasAdded()) {
          for (var added : c.getAddedSubList()) {
            var diagramBlock = new DiagramBlock(this, added, blockCache);
            blocks.getChildren().add(diagramBlock);
          }
        }
      }
    });
  }
}
