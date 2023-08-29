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

import java.util.EventListener;
import java.util.function.*;
import java.util.stream.Stream;

import static java.util.stream.Stream.empty;
import static java.util.stream.Stream.iterate;

public final class UniEventListeners<L extends EventListener> {

  private Item<L> item;

  public void add(L listener) {
    item = new Item<>(listener, item);
  }

  public void remove(L listener) {
    if (item == null) {
      return;
    } else if (item.listener == listener) {
      item = item.next;
      return;
    }
    for (var i = item; i.next != null; i = i.next) {
      if (i.next.listener == listener) {
        i.next = i.next.next;
        return;
      }
    }
  }

  public void forEach(Consumer<L> consumer) {
    for (var i = item; i != null; i = i.next) {
      consumer.accept(i.listener);
    }
  }

  public int size() {
    int size = 0;
    for (var i = item; i != null; i = i.next) {
      size++;
    }
    return size;
  }

  public Stream<L> stream() {
    return item == null ? empty() : iterate(item, i -> i.next != null, i -> i.next).map(i -> i.listener);
  }

  private static final class Item<L> {

    private final L listener;
    private Item<L> next;

    private Item(L listener, Item<L> next) {
      this.listener = listener;
      this.next = next;
    }
  }
}
