package org.tybaco.model;

/*-
 * #%L
 * library
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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.w3c.dom.Element;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public final class In extends AbstractModelElement {

  private final Type type;
  private final int id;
  private final String name;

  void save(Element element) {
    element.setAttribute("type", type.name());
    element.setAttribute("id", Integer.toString(id));
    element.setAttribute("name", name);
    saveAttributes(element);
  }

  static In load(Element element) {
    var type = Type.valueOf(element.getAttribute("type"));
    var id = Integer.parseInt(element.getAttribute("id"));
    var name = element.getAttribute("name");
    var in = new In(type, id, name);
    in.loadAttributes(element);
    return in;
  }

  @Override
  public int hashCode() {
    return type.hashCode() ^ name.hashCode() ^ id;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof In i && type == i.type && name.equals(i.name) && id == i.id;
  }

  @Override
  public String toString() {
    return type + "[" + id + "](" + name + ")";
  }

  public enum Type {
    ARG,
    INPUT
  }
}
