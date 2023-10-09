package org.tybaco.ui.model;

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
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;

import java.util.*;
import java.util.stream.Stream;

import static java.nio.ByteBuffer.allocate;
import static java.util.Base64.getUrlEncoder;
import static java.util.Collections.emptyList;
import static org.tybaco.xml.Xml.elementsByTag;
import static org.tybaco.xml.Xml.withChildren;

public final class Project {

  public final String id;
  public final ThreadGroup threadGroup;
  public final SimpleStringProperty name;
  public final ObservableList<Block> blocks;
  public final ObservableList<Constant> constants;
  public final ObservableSet<Link> links;
  public final ObservableList<Dependency> dependencies;
  private final Observable[] observables;

  Project(String id, String name, Collection<Constant> constants, Collection<Block> blocks, Collection<Link> links, Collection<Dependency> dependencies) {
    this.id = id == null ? newId() : id;
    this.threadGroup = new ThreadGroup(id);
    this.name = new SimpleStringProperty(this, "name", name);
    this.constants = Constant.newList(constants);
    this.blocks = Block.newList(blocks);
    this.links = FXCollections.observableSet(new HashSet<>(links));
    this.dependencies = Dependency.libs(dependencies);
    this.observables = new Observable[] {this.name, this.constants, this.blocks, this.dependencies};
  }

  public Project(String name) {
    this(null, name, emptyList(), emptyList(), emptyList(), emptyList());
  }

  public Project(Element element) {
    this(
      element.getAttribute("id"),
      element.getAttribute("name"),
      elementsByTag(element, "constant").map(Constant::new).toList(),
      elementsByTag(element, "block").map(Block::new).toList(),
      elementsByTag(element, "link").map(Link::new).toList(),
      elementsByTag(element, "dependency").map(Dependency::new).toList()
    );
  }

  public void saveTo(Element element) {
    element.setAttribute("id", id);
    element.setAttribute("name", name.get());
    withChildren(element, "constant", constants, Constant::saveTo);
    withChildren(element, "block", blocks, Block::saveTo);
    withChildren(element, "link", links, Link::saveTo);
    withChildren(element, "dependency", dependencies, Dependency::saveTo);
  }

  private Observable[] observables() {
    return observables;
  }

  public static ObservableList<Project> newList(Collection<Project> projects) {
    return FXCollections.observableList(new ArrayList<>(projects), Project::observables);
  }

  public static ObservableList<Project> newList() {
    return FXCollections.observableArrayList(Project::observables);
  }

  public Block blockById(int id) {
    return blocks.stream()
      .filter(b -> b.id == id)
      .findFirst()
      .orElseThrow(() -> new NoSuchElementException("Block" + id + " not found"));
  }

  public Stream<Link> linksFrom(Connector out) {
    return links.stream().filter(l -> l.out.equals(out));
  }

  public Stream<Link> linksTo(Connector in) {
    return links.stream().filter(l -> l.in.equals(in));
  }

  public Stream<Link> linksFrom(Block block) {
    return links.stream().filter(l -> l.out.blockId == block.id);
  }

  public Stream<Link> linksTo(Block block) {
    return links.stream().filter(l -> l.in.blockId == block.id);
  }

  public Block newBlock(String name, String factoryId, double x, double y) {
    var block = new Block(nextId(), name, factoryId, x, y);
    blocks.add(block);
    return block;
  }

  public Constant newConstant(@NotNull String name, @NotNull String factoryId, @NotNull String value) {
    var constant = new Constant(nextId(), name, factoryId, value);
    constants.add(constant);
    return constant;
  }

  private int nextId() {
    var set = new BitSet();
    blocks.forEach(b -> set.set(b.id));
    constants.forEach(c -> set.set(c.id));
    return set.nextClearBit(0);
  }

  public String guessBlockName() {
    return "Block " + nextId();
  }

  private String newId() {
    var hash = System.identityHashCode(this);
    var time = System.currentTimeMillis() - 1_600_000_000_000L;
    var longId = (time << 32) | ((long) hash & 0xFFFF_FFFFL);
    return getUrlEncoder().withoutPadding().encodeToString(allocate(8).putLong(0, longId).array());
  }

  @Override
  public String toString() {
    return name.get() + " (" + id + ")";
  }
}
