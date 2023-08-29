package org.tybaco.ui.main.projects;

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

import org.tybaco.xml.Xml;
import org.tybaco.ui.lib.id.Ids;
import org.tybaco.ui.lib.props.*;
import org.w3c.dom.Element;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

import static org.tybaco.xml.Xml.elementsByTags;
import static org.tybaco.xml.Xml.withChild;

public final class Project extends AbstractEntity {

  private final BitSet blockIds = new BitSet();
  public final String id;
  public final Prop<String> name;
  public final ListProp<Library> libs;
  public final ListProp<Block> blocks;

  public Project(String name) {
    this(Ids.newId(), name, List.of(), List.of());
  }

  private Project(String id, String name, Collection<Library> libs, Collection<Block> blocks) {
    this.id = id;
    this.name = new Prop<>(this, "name", name);
    this.libs = new ListProp<>(this, "libs", libs);
    this.blocks = new ListProp<>(this, "blocks", blocks);
    blocks.forEach(b -> blockIds.set(b.id));
  }

  public static Project loadFrom(Element element) {
    return new Project(
      element.getAttribute("id"),
      element.getAttribute("name"),
      elementsByTags(element, "libs", "lib").map(Library::loadFrom).toList(),
      elementsByTags(element, "blocks", "block").map(Block::loadFrom).toList()
    );
  }

  public static Project loadFrom(Path path) {
    return Xml.loadFrom(path, Project::loadFrom);
  }

  public void saveTo(Element element) {
    element.setAttribute("id", id);
    element.setAttribute("name", name.get());
    withChild(element, "libs", libs -> this.libs.forEach(l -> withChild(libs, "lib", l::saveTo)));
    withChild(element, "blocks", blocks -> this.blocks.forEach(b -> withChild(blocks, "block", b::saveTo)));
  }

  public void saveTo(Path path) {
    Xml.saveTo(path, "project", this::saveTo);
  }

  public Block addBlock(String factory, String method, String name) {
    var block = new Block(blockIds.nextClearBit(0), factory, method, name);
    blockIds.set(block.id);
    return block;
  }

  public void addBlock(String factory, String method, String name, Consumer<Block> consumer) {
    consumer.accept(addBlock(factory, method, name));
  }
}
