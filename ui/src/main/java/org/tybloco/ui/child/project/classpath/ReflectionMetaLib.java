package org.tybloco.ui.child.project.classpath;

/*-
 * #%L
 * ui
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

import org.tybloco.editors.Meta;
import org.tybloco.editors.model.MetaLib;

import java.lang.annotation.Annotation;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Stream;

public abstract class ReflectionMetaLib<E extends Meta, L extends ReflectionMetaLib<E, L>> implements MetaLib {

  private final String id;
  private final String name;
  private final String icon;
  private final String description;
  final ConcurrentSkipListMap<String, E> children = new ConcurrentSkipListMap<>();
  final ConcurrentSkipListMap<String, L> libs = new ConcurrentSkipListMap<>();

  ReflectionMetaLib(String id, Annotation annotation) {
    try {
      this.id = id;
      this.name = annotation.annotationType().getMethod("name").invoke(annotation).toString();
      this.icon = annotation.annotationType().getMethod("icon").invoke(annotation).toString();
      this.description = annotation.annotationType().getMethod("description").invoke(annotation).toString();
    } catch (ReflectiveOperationException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String icon() {
    return icon;
  }

  @Override
  public String description() {
    return description;
  }

  @Override
  public Stream<? extends E> children() {
    return children.values().stream();
  }

  @Override
  public Stream<? extends L> childLibs() {
    return libs.values().stream();
  }
}
