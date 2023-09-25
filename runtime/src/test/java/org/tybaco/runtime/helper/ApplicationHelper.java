package org.tybaco.runtime.helper;

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

import org.tybaco.runtime.application.*;

import java.lang.reflect.Method;

public interface ApplicationHelper {

  static ApplicationConnector out(int block) {
    return new ApplicationConnector(block, "*", -1);
  }

  static ApplicationConnector out(int block, String spot) {
    return new ApplicationConnector(block, spot, -1);
  }

  static ApplicationConnector in(int block, String spot) {
    return new ApplicationConnector(block, spot, -1);
  }

  static ApplicationConnector in(int block, String spot, int index) {
    return new ApplicationConnector(block, spot, index);
  }

  static ApplicationLink arg(ApplicationConnector out, ApplicationConnector in) {
    return new ApplicationLink(out, in, true);
  }

  static ApplicationLink inp(ApplicationConnector out, ApplicationConnector in) {
    return new ApplicationLink(out, in, false);
  }

  static ApplicationBlock block(int id, Method method) {
    var factory = method.getDeclaringClass().getName();
    var value = method.getName();
    return new ApplicationBlock(id, factory, value);
  }

  static ApplicationBlock block(int id, Class<?> type) {
    return new ApplicationBlock(id, type.getName(), "new");
  }

  static ApplicationConstant constant(int id, String factory, String value) {
    return new ApplicationConstant(id, factory, value);
  }
}
