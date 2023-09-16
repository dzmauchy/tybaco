package org.tybaco.ui.child.project.classpath;

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

import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public final class ProjectCache implements AutoCloseable {

  final ConcurrentHashMap<Class<?>, ConcurrentLinkedQueue<Method>> blocks = new ConcurrentHashMap<>(64, 0.5f);
  final ConcurrentHashMap<Class<?>, Boolean> constants = new ConcurrentHashMap<>(64, 0.5f);
  final ConcurrentHashMap<Class<?>, ConcurrentLinkedQueue<Method>> inputs = new ConcurrentHashMap<>(64, 0.5f);

  void clear() {
    blocks.clear();
    constants.clear();
    inputs.clear();
  }

  @Override
  public void close() {
    clear();
  }
}
