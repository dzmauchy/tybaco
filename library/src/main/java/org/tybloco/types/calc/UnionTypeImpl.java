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

import java.lang.reflect.Type;
import java.util.Set;
import java.util.stream.Collectors;

record UnionTypeImpl(Set<? extends Type> types) implements UnionType {

  @Override
  public int hashCode() {
    return types.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof UnionType u && types.equals(u.types());
  }

  @Override
  public Type[] getTypes() {
    return types.toArray(Type[]::new);
  }

  @Override
  public String getTypeName() {
    return types.stream().map(Type::getTypeName).collect(Collectors.joining(" | "));
  }

  @Override
  public String toString() {
    return getTypeName();
  }
}
