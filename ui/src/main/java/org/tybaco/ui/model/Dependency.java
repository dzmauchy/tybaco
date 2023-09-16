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

public record Dependency(String group, String artifact, String version) {

  public Dependency(Element element) {
    this(
      element.getAttribute("group"),
      element.getAttribute("artifact"),
      element.getAttribute("version")
    );
  }

  public void saveTo(Element element) {
    element.setAttribute("group", group);
    element.setAttribute("artifact", artifact);
    element.setAttribute("version", version);
  }

  public static ObservableList<Dependency> libs(Collection<Dependency> dependencies) {
    return FXCollections.observableArrayList(dependencies);
  }
}
