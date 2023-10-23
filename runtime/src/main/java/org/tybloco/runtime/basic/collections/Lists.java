package org.tybloco.runtime.basic.collections;

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

import org.tybloco.runtime.meta.*;

import java.util.List;

@Blocks(name = "Lists", icon = "順", description = "Lists")
public interface Lists {

  @SafeVarargs
  @Block(name = "Immutable list", icon = "順", description = "Immutable list of elements")
  static <E> List<E> immutableList(
    @Input(name = "Elements", icon = "単", description = "Elements")
    E... elements
  ) {
    return List.of(elements);
  }
}
