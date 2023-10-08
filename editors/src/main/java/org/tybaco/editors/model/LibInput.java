package org.tybaco.editors.model;

/*-
 * #%L
 * editors
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

import org.tybaco.editors.Meta;

public record LibInput(String name, String icon, String description, boolean vector, boolean optional) implements Meta {

  public static LibInput required(String name, String icon, String description) {
    return new LibInput(name, icon, description, false, false);
  }

  public static LibInput optional(String name, String icon, String description) {
    return new LibInput(name, icon, description, false, true);
  }

  public static LibInput vector(String name, String icon, String description) {
    return new LibInput(name, icon, description, true, true);
  }
}
