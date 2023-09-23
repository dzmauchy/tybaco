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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.NoSuchElementException;

import static org.tybaco.xml.Xml.elementByTag;
import static org.tybaco.xml.Xml.withChild;

public record Link(Connector out, Connector in, boolean arg) {

  public Link(Element element) {
    this(
      new Connector(elementByTag(element, "out").orElseThrow(() -> new NoSuchElementException("out"))),
      new Connector(elementByTag(element, "in").orElseThrow(() -> new NoSuchElementException("in"))),
      switch (element.getAttribute("arg")) {
        case "", "true" -> true;
        default -> false;
      }
    );
  }

  public void saveTo(Element element) {
    withChild(element, "out", out::saveTo);
    withChild(element, "in", in::saveTo);
    if (!arg) {
      element.setAttribute("arg", "false");
    }
  }

  public static ObservableList<Link> newList(Collection<Link> links) {
    return FXCollections.observableArrayList(links);
  }
}
