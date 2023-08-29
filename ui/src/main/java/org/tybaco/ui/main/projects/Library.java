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

import org.w3c.dom.Element;

public record Library(String group, String name, String version) {

  static Library loadFrom(Element element) {
    return new Library(
      element.getAttribute("group"),
      element.getAttribute("name"),
      element.getAttribute("version")
    );
  }

  void saveTo(Element element) {
    element.setAttribute("group", group);
    element.setAttribute("name", name);
    element.setAttribute("version", version);
  }
}
