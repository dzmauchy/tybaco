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

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

public final class ClassInfoCache extends ClassValue<ClassInfo> {

  @Override
  protected ClassInfo computeValue(Class<?> type) {
    var inputs = new HashMap<String, Method>();
    var outputs = new HashMap<String, Method>();
    var factories = new HashMap<String, FactoryInfo>();
    for (var c : type.getConstructors()) {
      factories.compute("new", (k, o) -> merge(o, c));
    }
    for (var m : type.getMethods()) {
      if (Modifier.isStatic(m.getModifiers())) {
        if (m.getReturnType() != void.class) {
          factories.compute(m.getName(), (k, o) -> merge(o, m));
        }
      } else {
        if (m.getParameterCount() == 1) {
          if (m.getReturnType() == void.class) {
            inputs.put(m.getName(), m);
          }
        } else if (m.getParameterCount() == 0) {
          if (m.getReturnType() != void.class) {
            outputs.put(m.getName(), m);
          }
        }
      }
    }
    return new ClassInfo(Map.copyOf(inputs), Map.copyOf(outputs), Map.copyOf(factories));
  }

  private static FactoryInfo merge(FactoryInfo old, Executable e) {
    return old != null && old.parameterCount() >= e.getParameterCount() ? old : new FactoryInfo(e);
  }
}
