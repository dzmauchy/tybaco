package org.tybloco.ui.child.project.diagram;

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

import javafx.collections.*;
import org.springframework.stereotype.Component;
import org.tybloco.ui.child.project.classpath.BlockCache;
import org.tybloco.ui.child.project.classpath.ProjectClasspath;
import org.tybloco.ui.child.project.diagram.line.DiagramLine;
import org.tybloco.ui.model.*;

@Component
public class Diagram extends AbstractDiagram {

  private final SetChangeListener<Link> linkListener = this::onLinkChange;
  private final ListChangeListener<Block> blockListener = this::onBlockChange;
  public final Project project;
  final BlockCache blockCache;
  final ProjectClasspath classpath;

  public Diagram(Project project, BlockCache blockCache, ProjectClasspath classpath) {
    this.project = project;
    this.blockCache = blockCache;
    this.classpath = classpath;
    initialize();
  }

  private void initialize() {
    project.links.addListener(new WeakSetChangeListener<>(linkListener));
    project.blocks.addListener(new WeakListChangeListener<>(blockListener));
    project.blocks.forEach(b -> onBlock(b, true));
    project.links.forEach(l -> onLink(l, true));
    blockCache.addListener(o -> {
      blocks.getChildren().clear();
      project.blocks.forEach(b -> onBlock(b, true));
      project.links.forEach(l -> notifyBlockLink(l, true));
    });
  }

  private void onLinkChange(SetChangeListener.Change<? extends Link> c) {
    if (c.wasAdded()) {
      onLink(c.getElementAdded(), true);
    } else {
      onLink(c.getElementRemoved(), false);
    }
  }

  private void onBlockChange(ListChangeListener.Change<? extends Block> c) {
    while (c.next()) {
      if (c.wasRemoved()) {
        c.getRemoved().forEach(b -> onBlock(b, false));
      }
      if (c.wasAdded()) {
        c.getAddedSubList().forEach(b -> onBlock(b, true));
      }
    }
  }

  private void onBlock(Block block, boolean add) {
    if (add) {
      blocks.getChildren().add(new DiagramBlock(this, block));
    } else {
      blocks.getChildren().removeIf(n -> n instanceof DiagramBlock b && b.block == block);
    }
  }

  private void onLink(Link link, boolean add) {
    if (add) {
      connectors.getChildren().add(new DiagramLine(this, link));
    } else {
      connectors.getChildren().removeIf(n -> n instanceof DiagramLine l && l.link == link);
    }
    notifyBlockLink(link, add);
  }

  private void notifyBlockLink(Link link, boolean added) {
    blocks.getChildren().forEach(n -> {
      if (n instanceof DiagramBlock b && (b.block.id == link.out.blockId || b.block.id == link.in.blockId)) {
        b.onLink(link, added);
      }
    });
  }
}
