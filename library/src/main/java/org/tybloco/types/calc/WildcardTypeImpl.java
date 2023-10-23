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
import java.lang.reflect.WildcardType;
import java.util.Arrays;

record WildcardTypeImpl(Type[] lb, Type[] ub) implements WildcardType {

  public static final WildcardTypeImpl ANY = new WildcardTypeImpl(Types.EMPTY_TYPES, new Type[]{Object.class});

  @Override
  public Type[] getUpperBounds() {
    return ub;
  }

  @Override
  public Type[] getLowerBounds() {
    return lb;
  }

  @Override
  public int hashCode() {
    return Arrays.asList(lb).hashCode() ^ Arrays.asList(ub).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof WildcardType w && Arrays.equals(ub, w.getUpperBounds()) && Arrays.equals(lb, w.getLowerBounds());
  }

  @Override
  public String getTypeName() {
    if (lb.length == 0 && (ub.length == 0 || ub[0] == Object.class)) {
      return "?";
    } else {
      var builder = new StringBuilder(32);
      builder.append('?');
      if (ub.length != 0) {
        builder.append(" extends ").append(ub[0].getTypeName());
        for (int i = 1; i < ub.length; i++) {
          builder.append(" & ").append(ub[i].getTypeName());
        }
      }
      if (lb.length != 0) {
        builder.append(" super ").append(lb[0].getTypeName());
        for (int i = 1; i < lb.length; i++) {
          builder.append(" & ").append(lb[i].getTypeName());
        }
      }
      return builder.toString();
    }
  }

  @Override
  public String toString() {
    return getTypeName();
  }
}
