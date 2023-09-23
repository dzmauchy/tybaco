package org.tybaco.runtime.application;

/*-
 * #%L
 * runtime
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

import java.lang.reflect.Method;

import static java.lang.Integer.parseInt;

public record ApplicationBlock(int id, String factory, String method) implements ResolvableObject {

  public ApplicationBlock(Element element) {
    this(
      parseInt(element.getAttribute("id")),
      element.getAttribute("factory"),
      element.getAttribute("method")
    );
  }

  public boolean isDependent() {
    return factory.chars().allMatch(Character::isDigit);
  }

  public int parentBlockId() {
    return parseInt(factory);
  }

  public static ApplicationBlock fromMethod(int id, Method method) {
    var factory = method.getDeclaringClass().getName();
    var value = method.getName();
    return new ApplicationBlock(id, factory, value);
  }
}
