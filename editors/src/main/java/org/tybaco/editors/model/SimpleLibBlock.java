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

import org.tybaco.editors.value.StringValue;
import org.tybaco.editors.value.Value;

public interface SimpleLibBlock extends LibConst {

  default String extractValue(Value value) {
    return switch (value) {
      case StringValue(var v) -> v;
      default -> throw new IllegalArgumentException(String.valueOf(value));
    };
  }
}
