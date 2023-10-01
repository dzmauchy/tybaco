package org.tybaco.editors.value;

/*-
 * #%L
 * editors
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

import org.tybaco.xml.Xml;
import org.w3c.dom.Element;

import java.util.List;

public record ArrayValue(List<Value> values) implements Value {

  public ArrayValue(Element element) {
    this(Xml.elementsByTag(element, "element").map(Value::load).toList());
  }

  @Override
  public void save(Element element) {
    element.setAttribute("type", "array");
    Xml.withChildren(element, "element", values, Value::save);
  }

  @Override
  public String toString() {
    return values.toString();
  }
}
