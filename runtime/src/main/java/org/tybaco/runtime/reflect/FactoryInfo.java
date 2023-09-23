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
import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;

public final class FactoryInfo {

  private final Executable executable;
  private final Parameter[] parameters;
  private final Map<String, Parameter> parameterMap;

  public FactoryInfo(Executable executable) {
    this.executable = executable;
    this.parameters = executable.getParameters();
    this.parameterMap = stream(parameters).collect(toUnmodifiableMap(Parameter::getName, identity()));
  }

  public Parameter parameter(String name) {
    return parameterMap.get(name);
  }

  public int parameterCount() {
    return parameters.length;
  }

  public void execute(Object bean, Map<String, Object> values) throws ReflectiveOperationException {
    var args = new Object[parameters.length];
    for (int i = 0; i < args.length; i++) {
      var param = parameters[i];
      var value = values.get(param.getName());
      if (value == null && param.getType().isPrimitive()) {
        value = Array.get(Array.newInstance(param.getType(), 1), 0);
      }
      args[i] = value;
    }
    switch (executable) {
      case Constructor<?> c -> c.newInstance(args);
      case Method m -> m.invoke(bean, args);
    }
  }

  public static Object defaultValue(Parameter parameter) {
    if (parameter.getType().isPrimitive()) {
      return Array.get(Array.newInstance(parameter.getType(), 1), 0);
    } else if (parameter.isVarArgs()) {
      return Array.newInstance(parameter.getType().getComponentType(), 0);
    } else {
      return null;
    }
  }
}
