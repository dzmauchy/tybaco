package org.tybaco.runtime.util;

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

import java.util.function.Consumer;

public final class FList<E> {

  private Item<E> first;
  private Item<E> last;

  public void add(E e) {
    var item = new Item<>(e, null);
    if (first == null) {
      first = last = item;
    } else {
      last.next = item;
      last = item;
    }
  }

  public void pollEach(Consumer<? super E> consumer) {
    last = null;
    var i = first;
    first = null;
    while (i != null) {
      var e = i.e;
      var n = i.next;
      i.next = null;
      i = n;
      consumer.accept(e);
    }
  }

  public boolean isEmpty() {
    return first == null && last == null;
  }

  private static final class Item<E> {

    private final E e;
    private Item<E> next;

    private Item(E e, Item<E> next) {
      this.e = e;
      this.next = next;
    }
  }
}
