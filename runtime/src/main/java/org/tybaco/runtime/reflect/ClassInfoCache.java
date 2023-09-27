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

public final class ClassInfoCache {

  private final HashMap<Class<?>, ClassInfo> map = new HashMap<>(64, 0.5f);

  public ClassInfo get(Class<?> type) {
    return map.computeIfAbsent(type, this::computeValue);
  }

  private ClassInfo computeValue(Class<?> type) {
    var methods = type.getMethods();
    var inputs = HashMap.<String, Method>newHashMap(methods.length);
    var outputs = HashMap.<String, Method>newHashMap(methods.length);
    var factories = HashMap.<String, FactoryInfo>newHashMap(methods.length);
    var staticFactories = HashMap.<String, FactoryInfo>newHashMap(methods.length);
    for (var c : type.getConstructors()) {
      if (!c.trySetAccessible()) continue;
      staticFactories.computeIfAbsent("new", k -> new FactoryInfo(c));
    }
    for (var m : methods) {
      if (!m.trySetAccessible()) continue;
      if (Modifier.isStatic(m.getModifiers())) {
        if (m.getReturnType() != void.class) {
          staticFactories.computeIfAbsent(m.getName(), k -> new FactoryInfo(m));
        }
      } else {
        if (m.getReturnType() == void.class && m.getParameterCount() == 1) {
          inputs.put(m.getName(), m);
        } else if (m.getReturnType() != void.class && m.getParameterCount() == 0) {
          outputs.put(m.getName(), m);
        }
        if (m.getReturnType() != void.class) {
          factories.computeIfAbsent(m.getName(), k -> new FactoryInfo(m));
        }
      }
    }
    return new ClassInfo(
      type,
      Map.copyOf(inputs),
      Map.copyOf(outputs),
      Map.copyOf(factories),
      Map.copyOf(staticFactories)
    );
  }
}
