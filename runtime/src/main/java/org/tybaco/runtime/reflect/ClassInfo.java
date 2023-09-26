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

import org.tybaco.runtime.exception.*;

import java.lang.reflect.Method;
import java.util.Map;

public record ClassInfo(
  Class<?> type,
  Map<String, Method> inputs,
  Map<String, Method> outputs,
  Map<String, FactoryInfo> factories,
  Map<String, FactoryInfo> staticFactories
) {

  public Method input(String name) {
    var input = inputs.get(name);
    if (input == null) {
      throw new NoSuchInputException(name);
    } else {
      return input;
    }
  }

  public Method output(String name) {
    var output = outputs.get(name);
    if (output == null) {
      throw new NoSuchOutputException(name);
    } else {
      return output;
    }
  }

  public FactoryInfo factory(String name) {
    var factory = factories.get(name);
    if (factory == null) {
      throw new NoSuchFactoryException(name);
    } else {
      return factory;
    }
  }

  public FactoryInfo staticFactory(String name) {
    var factory = staticFactories.get(name);
    if (factory == null) {
      throw new NoSuchFactoryException(name);
    } else {
      return factory;
    }
  }
}
