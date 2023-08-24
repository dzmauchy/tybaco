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

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class Block extends AbstractModelElement {

  private final int id;
  private final String type;
  private final String method;

  void save(Element element) {
    element.setAttribute("id", Integer.toString(id));
    element.setAttribute("type", type);
    element.setAttribute("method", method);
    saveAttributes(element);
  }

  static Block load(Element element) {
    var id = Integer.parseInt(element.getAttribute("id"));
    var type = element.getAttribute("type");
    var method = element.getAttribute("method");
    var block = new Block(id, type, method);
    block.loadAttributes(element);
    return block;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Block b && id == b.id && type.equals(b.type) && method.equals(b.method);
  }

  @Override
  public int hashCode() {
    return id ^ type.hashCode() ^ method.hashCode();
  }

  @Override
  public String toString() {
    return "Block(" + id + "," + type + "." + method + ")";
  }
}
