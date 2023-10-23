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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

record ParameterizedTypeImpl(Type owner, Type raw, Type[] args) implements ParameterizedType {

  @Override
  public Type[] getActualTypeArguments() {
    return args;
  }

  @Override
  public Type getRawType() {
    return raw;
  }

  @Override
  public Type getOwnerType() {
    return owner;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(owner) ^ Arrays.asList(args).hashCode() ^ raw.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ParameterizedType p
      && Objects.equals(owner, p.getOwnerType())
      && raw.equals(p.getRawType())
      && Arrays.equals(args, p.getActualTypeArguments());
  }

  @Override
  public String getTypeName() {
    var builder = new StringBuilder(64);
    if (owner != null) {
      builder.append(owner.getTypeName()).append('.');
    }
    builder.append(raw.getTypeName()).append("<");
    if (args.length > 0) {
      builder.append(args[0].getTypeName());
      for (int i = 1; i < args.length; i++) {
        builder.append(',').append(args[i].getTypeName());
      }
    }
    builder.append('>');
    return builder.toString();
  }

  @Override
  public String toString() {
    return getTypeName();
  }
}
