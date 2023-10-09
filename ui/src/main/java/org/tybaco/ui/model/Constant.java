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
import org.tybaco.xml.Xml;
import org.w3c.dom.Element;

import java.util.*;

public final class Constant {

  public final int id;
  public final SimpleStringProperty name;
  public final String factoryId;
  public final SimpleObjectProperty<String> value;
  private final Observable[] observables;

  Constant(int id, String name, String factoryId, String value) {
    this.id = id;
    this.name = new SimpleStringProperty(this, "name", name);
    this.factoryId = factoryId;
    this.value = new SimpleObjectProperty<>(this, "value", value);
    this.observables = new Observable[] {this.name, this.value};
  }

  public Constant(Element element) {
    this(
      Integer.parseInt(element.getAttribute("id")),
      element.getAttribute("name"),
      element.getAttribute("factoryId"),
      Xml.elementByTag(element, "value").map(Element::getTextContent).orElse("")
    );
  }

  public void saveTo(Element element) {
    element.setAttribute("id", Integer.toString(id));
    element.setAttribute("name", name.get());
    element.setAttribute("factoryId", factoryId);
    Xml.withChild(element, "value", e -> e.setTextContent(value.get()));
  }

  public static ObservableList<Constant> newList(Collection<Constant> constants) {
    return FXCollections.observableList(new ArrayList<>(constants), c -> c.observables);
  }
}
