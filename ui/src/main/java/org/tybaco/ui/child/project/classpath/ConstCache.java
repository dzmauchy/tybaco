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

import javafx.beans.property.SimpleObjectProperty;
import org.springframework.stereotype.Component;
import org.tybaco.editors.model.LibConst;

import java.util.*;

@Component
public final class ConstCache {

  public final SimpleObjectProperty<Map<String, LibConst>> cache = new SimpleObjectProperty<>(this, "cache", Map.of());

  public ConstCache(Editors editors) {
    editors.constLibs.addListener((o, ov, nv) -> {
      if (nv == null) {
        cache.set(Map.of());
        return;
      }
      var count = nv.stream().mapToInt(l -> l.constants().size()).sum();
      var map = HashMap.<String, LibConst>newHashMap(count);
      nv.forEach(l -> l.constants().forEach(c -> map.put(c.id(), c)));
      cache.set(Map.copyOf(map));
    });
  }

  public Optional<LibConst> constById(String id) {
    return Optional.ofNullable(cache.get().get(id));
  }
}
