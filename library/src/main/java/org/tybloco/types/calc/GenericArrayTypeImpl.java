package org.tybloco.types.calc;

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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

record GenericArrayTypeImpl(Type componentType) implements GenericArrayType {

  @Override
  public Type getGenericComponentType() {
    return componentType;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof GenericArrayType a && componentType.equals(a.getGenericComponentType());
  }

  @Override
  public int hashCode() {
    return componentType.hashCode();
  }

  @Override
  public String getTypeName() {
    return componentType.getTypeName() + "[]";
  }

  @Override
  public String toString() {
    return getTypeName();
  }
}
