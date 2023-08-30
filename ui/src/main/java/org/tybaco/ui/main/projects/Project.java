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

import org.tybaco.ui.lib.id.Ids;
import org.tybaco.ui.lib.props.*;
import org.tybaco.xml.Xml;
import org.w3c.dom.Element;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.tybaco.xml.Xml.elementsByTags;
import static org.tybaco.xml.Xml.withChild;

public final class Project extends AbstractEntity {

  private final BitSet blockIds = new BitSet();
  public final String id;
  private final Prop<String> name;
  private final ListProp<Library> libs;
  private final ListProp<Block> blocks;
  private final ListProp<Link> links;

  public Project(String name) {
    this(Ids.newId(), name, List.of(), List.of(), List.of());
  }

  private Project(String id, String name, Collection<Library> libs, Collection<Block> blocks, Collection<Link> links) {
    this.id = id;
    this.name = new Prop<>(this, "name", name);
    this.libs = new ListProp<>(this, "libs", libs);
    this.blocks = new ListProp<>(this, "blocks", blocks);
    this.links = new ListProp<>(this, "links", links);
    blocks.forEach(b -> blockIds.set(b.id));
  }

  public static Project loadFrom(Element element) {
    return new Project(
      element.getAttribute("id"),
      element.getAttribute("name"),
      elementsByTags(element, "libs", "lib").map(Library::loadFrom).toList(),
      elementsByTags(element, "blocks", "block").map(Block::loadFrom).toList(),
      elementsByTags(element, "links", "link").map(Link::loadFrom).toList()
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
    withChild(element, "links", links -> this.links.forEach(l -> withChild(links, "link", l::saveTo)));
  }

  public void saveTo(Path path) {
    Xml.saveTo(path, "project", this::saveTo);
  }

  public Block addBlock(String factory, String method, String name) {
    var block = new Block(blockIds.nextClearBit(0), factory, method, name);
    blockIds.set(block.id);
    return block;
  }

  public Block addBlock(String factory, String method, String name, Consumer<Block> consumer) {
    var block = addBlock(factory, method, name);
    consumer.accept(block);
    return block;
  }

  public Link addLink(Connector out, Connector in) {
    var link = new Link(out, in);
    links.add(link);
    return link;
  }

  public void addLink(Link link) {
    links.add(link);
  }

  public Block removeBlock(int index) {
    return blocks.remove(index);
  }

  public List<Block> removeBlocks(Predicate<Block> predicate) {
    return blocks.removeAll(predicate);
  }

  public void removeBlock(Block block) {
    blocks.remove(block);
  }

  public void removeLink(Link link) {
    links.remove(link);
  }

  public List<Link> removeLinks(Predicate<Link> predicate) {
    return links.removeAll(predicate);
  }

  public Stream<Link> linksFrom(Connector out) {
    return links.findAll(l -> l.out().equals(out));
  }

  public Stream<Link> linksTo(Connector in) {
    return links.findAll(l -> l.in().equals(in));
  }

  public String getName() {
    return name.get();
  }

  public void setName(String name) {
    this.name.set(name);
  }
}
