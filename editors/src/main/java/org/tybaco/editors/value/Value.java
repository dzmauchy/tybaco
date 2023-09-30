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

import org.w3c.dom.Element;

public sealed interface Value permits StringValue, ArrayValue, MapValue, NullValue {

  void save(Element element);

  static Value load(Element element) {
    var type = element.getAttribute("type");
    return switch (type) {
      case "" -> new StringValue(element);
      case "array" -> new ArrayValue(element);
      case "map" -> new MapValue(element);
      case "null" -> NullValue.NULL;
      default -> throw new IllegalArgumentException("Invalid type: " + type);
    };
  }
}
