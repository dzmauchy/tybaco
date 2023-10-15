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

import org.springframework.stereotype.Component;
import org.tybaco.ui.child.project.classpath.BlockCache;
import org.tybaco.ui.child.project.classpath.ProjectClasspath;
import org.tybaco.ui.model.Project;

import static org.tybaco.editors.base.ObservableLists.synchronizeLists;
import static org.tybaco.editors.base.ObservableSets.synchronizeSet;

@Component
public class Diagram extends AbstractDiagram {

  public final Project project;
  final BlockCache blockCache;
  final ProjectClasspath classpath;
  private final Runnable resetBlocks;
  private final Runnable resetLinks;

  public Diagram(Project project, BlockCache blockCache, ProjectClasspath classpath) {
    this.project = project;
    this.blockCache = blockCache;
    this.classpath = classpath;
    this.resetLinks = synchronizeSet(project.links, connectors.getChildren(), l -> new DiagramLine(this, l), l -> l instanceof DiagramLine e ? e.link : null);
    this.resetBlocks = synchronizeLists(project.blocks, blocks.getChildren(), b -> new DiagramBlock(this, b));
    initialize();
  }

  private void initialize() {
    blockCache.addListener(o -> {
      resetBlocks.run();
      resetLinks.run();
    });
  }
}
