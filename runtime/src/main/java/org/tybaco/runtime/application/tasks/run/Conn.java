package org.tybaco.runtime.application.tasks.run;

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

import org.tybaco.runtime.application.ResolvableObject;

import static java.lang.System.identityHashCode;

public record Conn(ResolvableObject block, String spot) {

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Conn c && block == c.block && spot.equals(c.spot);
  }

  @Override
  public int hashCode() {
    return identityHashCode(block) ^ spot.hashCode();
  }

  @Override
  public String toString() {
    return block.id() + "(" + spot + ")";
  }
}
