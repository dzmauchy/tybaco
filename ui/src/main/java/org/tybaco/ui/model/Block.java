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
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collection;

import static java.lang.Float.parseFloat;

public final class Block {

  public final int id;
  public final SimpleStringProperty name;
  public final SimpleStringProperty factory;
  public final SimpleStringProperty value;
  public final SimpleObjectProperty<Point2D> pos;
  private final Observable[] observables;

  Block(int id, String name, String factory, String value, Point2D pos) {
    this.id = id;
    this.name = new SimpleStringProperty(this, "name", name);
    this.factory = new SimpleStringProperty(this, "factory", factory);
    this.value = new SimpleStringProperty(this, "value", value);
    this.pos = new SimpleObjectProperty<>(this, "pos", pos);
    this.observables = new Observable[] {this.name, this.factory, this.value};
  }

  public Block(Element element) {
    this(
      Integer.parseInt(element.getAttribute("id")),
      element.getAttribute("name"),
      element.getAttribute("factory"),
      element.getAttribute("value"),
      new Point2D(parseFloat(element.getAttribute("x")), parseFloat(element.getAttribute("y")))
    );
  }

  public void saveTo(Element element) {
    element.setAttribute("id", Integer.toString(id));
    element.setAttribute("name", name.get());
    element.setAttribute("factory", factory.get());
    element.setAttribute("value", value.get());
    element.setAttribute("x", Float.toString((float) pos.get().getX()));
    element.setAttribute("y", Float.toString((float) pos.get().getY()));
  }

  private Observable[] observables() {
    return observables;
  }

  public static ObservableList<Block> newList(Collection<Block> blocks) {
    return FXCollections.observableList(new ArrayList<>(blocks), Block::observables);
  }
}
