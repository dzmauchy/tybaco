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
import org.tybloco.editors.Meta;
import org.tybloco.editors.model.ConstLib;
import org.tybloco.editors.model.LibConst;

import java.util.*;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.tybaco.logging.Log.warn;

@Component
public final class ConstCache {

  public final SimpleObjectProperty<Map<String, LibConst>> cache = new SimpleObjectProperty<>(this, "cache", Map.of());

  public ConstCache(Editors editors) {
    editors.constLibs.addListener((o, ov, nv) -> cache.set(Stream.ofNullable(nv)
      .flatMap(Collection::stream)
      .flatMap(ConstLib::allConstants)
      .collect(toUnmodifiableMap(Meta::id, identity(), (c1, c2) -> {
        warn(ConstCache.class, "Duplicated constant {0}: {1}, {2}", c1.id(), c1, c2);
        return c2;
      }))
    ));
  }

  public Optional<LibConst> constById(String id) {
    return Optional.ofNullable(cache.get().get(id));
  }
}
