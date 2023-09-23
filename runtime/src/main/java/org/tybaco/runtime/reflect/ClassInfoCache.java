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
    var methods = type.getMethods();
    var inputs = new HashMap<String, Method>(methods.length);
    var outputs = new HashMap<String, Method>(methods.length);
    var factories = new HashMap<String, FactoryInfo>(methods.length);
    var staticFactories = new HashMap<String, FactoryInfo>(methods.length);
    for (var c : type.getConstructors()) {
      factories.compute("new", (k, o) -> merge(o, c));
    }
    for (var m : methods) {
      if (!m.trySetAccessible()) {
        continue;
      }
      if (Modifier.isStatic(m.getModifiers())) {
        if (m.getReturnType() != void.class) {
          staticFactories.compute(m.getName(), (k, o) -> merge(o, m));
        }
      } else {
        if (m.getReturnType() == void.class && m.getParameterCount() == 1) {
          inputs.put(m.getName(), m);
        } else if (m.getReturnType() != void.class && m.getParameterCount() == 0) {
          outputs.put(m.getName(), m);
        }
        if (m.getReturnType() != void.class) {
          factories.compute(m.getName(), (k, o) -> merge(o, m));
        }
      }
    }
    return new ClassInfo(type, Map.copyOf(inputs), Map.copyOf(outputs), Map.copyOf(factories), Map.copyOf(staticFactories));
  }

  private static FactoryInfo merge(FactoryInfo old, Executable e) {
    return old != null && old.parameterCount() >= e.getParameterCount() ? old : new FactoryInfo(e);
  }
}
