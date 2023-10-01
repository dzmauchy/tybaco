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

import org.jetbrains.annotations.NotNull;
import org.kordamp.ikonli.materialdesign2.MaterialDesignU;
import org.tybaco.editors.model.Descriptor;

import java.lang.reflect.AnnotatedElement;

public interface Meta extends Comparable<Meta> {

  default String id() {
    var descriptor = metaAnnotatedElement().getAnnotation(Descriptor.class);
    return descriptor != null ? descriptor.id() : defaultName();
  }

  default String name() {
    var descriptor = metaAnnotatedElement().getAnnotation(Descriptor.class);
    return descriptor != null ? descriptor.name() : defaultName();
  }

  default String icon() {
    var descriptor = metaAnnotatedElement().getAnnotation(Descriptor.class);
    return descriptor != null ? descriptor.icon() : MaterialDesignU.UFO.getDescription();
  }

  default String description() {
    var description = metaAnnotatedElement().getAnnotation(Descriptor.class);
    return description != null ? description.description() : "";
  }

  default AnnotatedElement metaAnnotatedElement() {
    return getClass();
  }

  private String defaultName() {
    return switch (metaAnnotatedElement()) {
      case Class<?> c -> c.getSimpleName();
      case Package p -> p.getName();
      default -> throw new IllegalArgumentException(metaAnnotatedElement().getClass().getName());
    };
  }

  @Override
  default int compareTo(@NotNull Meta o) {
    return name().compareTo(o.name());
  }

  static Meta meta(Package p) {
    return new Meta() {
      @Override
      public AnnotatedElement metaAnnotatedElement() {
        return p;
      }
    };
  }
}
