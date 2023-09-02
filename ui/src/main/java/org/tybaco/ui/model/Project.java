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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collection;

import static org.tybaco.xml.Xml.elementsByTags;
import static org.tybaco.xml.Xml.withChildren;

public final class Project {

  public final String id;
  public final SimpleStringProperty name;
  public final ObservableList<Block> blocks;
  public final ObservableList<Lib> libs;
  private final Observable[] observables;

  Project(String id, String name, Collection<Block> blocks, Collection<Lib> libs) {
    this.id = id;
    this.name = new SimpleStringProperty(this, "name", name);
    this.blocks = Block.newList(blocks);
    this.libs = Lib.libs(libs);
    this.observables = new Observable[] {this.name, this.blocks, this.libs};
  }

  public Project(Element element) {
    this(
      element.getAttribute("id"),
      element.getAttribute("name"),
      elementsByTags(element, "blocks", "block").map(Block::new).toList(),
      elementsByTags(element, "libs", "lib").map(Lib::new).toList()
    );
  }

  public void saveTo(Element element) {
    element.setAttribute("id", id);
    element.setAttribute("name", name.get());
    withChildren(element, "blocks", "block", blocks, Block::saveTo);
    withChildren(element, "libs", "lib", libs, Lib::saveTo);
  }

  private Observable[] observables() {
    return observables;
  }

  public static ObservableList<Project> newList(Collection<Project> projects) {
    return FXCollections.observableList(new ArrayList<>(projects), Project::observables);
  }
}
