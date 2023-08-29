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

import org.tybaco.ui.lib.props.AbstractEntity;
import org.tybaco.ui.lib.props.Prop;
import org.w3c.dom.Element;

public final class Block extends AbstractEntity {

  public final int id;
  public final String factory;
  public final String selector;
  public final Prop<String> name;

  Block(int id, String factory, String selector, String name) {
    this.id = id;
    this.factory = factory;
    this.selector = selector;
    this.name = new Prop<>(this, "name", name);
  }

  static Block loadFrom(Element element) {
    var id = Integer.parseInt(element.getAttribute("id"));
    var factory = element.getAttribute("factory");
    var method = element.getAttribute("selector");
    var name = element.getAttribute("name");
    return new Block(id, factory, method, name);
  }

  void saveTo(Element element) {
    element.setAttribute("id", Integer.toString(id));
    element.setAttribute("factory", factory);
    element.setAttribute("selector", selector);
    element.setAttribute("name", name.get());
  }
}
