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

import java.lang.reflect.Method;
import java.util.*;

public record ClassInfo(
  Class<?> type,
  Map<String, Method> inputs,
  Map<String, Method> outputs,
  Map<String, FactoryInfo> factories,
  Map<String, FactoryInfo> staticFactories
) {

  public Method input(String name) {
    return Objects.requireNonNull(inputs.get(name), () -> "No such input " + name + " for " + type);
  }

  public Method output(String name) {
    return Objects.requireNonNull(outputs.get(name), () -> "No such output " + name + " for " + type);
  }

  public FactoryInfo factory(String name) {
    return Objects.requireNonNull(factories.get(name), () -> "No such factory " + name + " for " + type);
  }

  public FactoryInfo staticFactory(String name) {
    return Objects.requireNonNull(staticFactories.get(name), () -> "No such static factory " + name + " for " + type);
  }
}
