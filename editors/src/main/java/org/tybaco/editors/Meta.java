package org.tybaco.editors;

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

import org.kordamp.ikonli.materialdesign2.MaterialDesignU;
import org.tybaco.editors.model.Descriptor;

public interface Meta {

  default String id() {
    var descriptor = getClass().getAnnotation(Descriptor.class);
    return descriptor != null ? descriptor.id() : getClass().getSimpleName();
  }

  default String name() {
    var descriptor = getClass().getAnnotation(Descriptor.class);
    return descriptor != null ? descriptor.name() : getClass().getSimpleName();
  }

  default String icon() {
    var descriptor = getClass().getAnnotation(Descriptor.class);
    return descriptor != null ? descriptor.icon() : MaterialDesignU.UFO.getDescription();
  }

  default String description() {
    var description = getClass().getAnnotation(Descriptor.class);
    return description != null ? description.description() : "";
  }
}
