package org.tybaco.runtime.application;

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

public record ApplicationConstant(int id, String factory, String value) implements ResolvableObject {

  public static ApplicationConstant fromClass(int id, Class<?> type, String value) {
    for (var method : type.getMethods()) {
      if (isFactoryExecutable(method)) {
        return new ApplicationConstant(id, type.getName(), value);
      }
    }
    for (var constructor : type.getConstructors()) {
      if (isFactoryExecutable(constructor)) {
        return new ApplicationConstant(id, type.getName(), value);
      }
    }
    throw new IllegalArgumentException("Unable to locate a method: " + type);
  }

  public static boolean isFactoryExecutable(Executable executable) {
    if (executable.getParameterCount() != 1) return false;
    else if (executable.getParameterTypes()[0] != String.class) return false;
    else if (executable instanceof Method m) return Modifier.isStatic(m.getModifiers()) && isConstantMethodName(m.getName());
    else return executable instanceof Constructor<?>;
  }

  public static boolean isConstantMethodName(String name) {
    return switch (name) {
      case "valueOf", "parse", "of", "getInstance", "instance", "instanceOf", "getByName", "byName", "forName" -> true;
      default -> false;
    };
  }
}
