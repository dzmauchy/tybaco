package org.tybaco.types.calc;

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

import java.lang.reflect.*;

final class TypeVars {

  private final TypeVariable<?> var;
  private TypeVars prev;

  TypeVars(TypeVariable<?> var, TypeVars prev) {
    this.var = var;
    this.prev = prev;
  }

  boolean contains(TypeVariable<?> var) {
    for (var v = this; v != null; v = v.prev) {
      if (var.equals(v.var)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    var builder = new StringBuilder("[");
    for (var v = this; v != null; v = v.prev) {
      var var = v.var;
      switch (var.getGenericDeclaration()) {
        case Class<?> c -> builder.append(c.getSimpleName());
        case Method m -> builder.append(m.getDeclaringClass().getSimpleName()).append('.').append(m.getName());
        case Constructor<?> c -> builder.append(c.getDeclaringClass().getSimpleName()).append(".new");
        default -> builder.append(var.getGenericDeclaration());
      }
      builder.append('<').append(var.getName()).append('>');
      if (v.prev != null) {
        builder.append(',');
      }
    }
    return builder.append(']').toString();
  }
}
