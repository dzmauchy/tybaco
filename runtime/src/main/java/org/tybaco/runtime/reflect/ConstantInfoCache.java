package org.tybaco.runtime.reflect;

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

import java.lang.reflect.Modifier;
import java.util.HashMap;

public final class ConstantInfoCache {

  private final HashMap<Class<?>, ConstantInfo> map = new HashMap<>(64, 0.5f);

  public ConstantInfo get(Class<?> type) {
    return map.computeIfAbsent(type, this::computeValue);
  }

  private ConstantInfo computeValue(Class<?> type) {
    for (var c : type.getConstructors()) {
      if (c.getParameterCount() == 1 && c.getParameterTypes()[0] == String.class) {
        return new ConstantInfo(c);
      }
    }
    for (var m : type.getMethods()) {
      if (Modifier.isStatic(m.getModifiers()) && m.getParameterCount() == 1 && m.getParameterTypes()[0] == String.class) {
        return new ConstantInfo(m);
      }
    }
    return null;
  }
}
