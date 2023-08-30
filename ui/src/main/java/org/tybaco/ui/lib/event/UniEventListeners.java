package org.tybaco.ui.lib.event;

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

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class UniEventListeners<L extends EventListener> {

  private final LinkedList<WeakReference<L>> refs = new LinkedList<>();

  public void add(L listener) {
    refs.removeIf(r -> r.get() == null);
    refs.add(new WeakReference<>(listener));
  }

  public void remove(L listener) {
    refs.removeIf(r -> r.get() == null || r.get() == listener);
  }

  public void forEach(Consumer<L> consumer) {
    for (var it = refs.listIterator(refs.size()); it.hasPrevious(); ) {
      var ref = it.previous();
      var l = ref.get();
      if (l == null) {
        it.remove();
      } else {
        consumer.accept(l);
      }
    }
  }

  public int size() {
    refs.removeIf(r -> r.get() == null);
    return refs.size();
  }

  public Stream<L> stream() {
    return refs.stream().map(WeakReference::get).filter(Objects::nonNull);
  }
}
