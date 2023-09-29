package org.tybaco.editors.util;

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

import java.util.*;
import java.util.function.BiConsumer;

import static java.util.Collections.unmodifiableSequencedSet;

public final class SeqMap<K, V> {

  private final LinkedHashMap<K, V> entries;

  @SafeVarargs
  public SeqMap(Map.Entry<K, V>... entries) {
    this.entries = LinkedHashMap.newLinkedHashMap(entries.length);
    for (var entry : entries) this.entries.put(entry.getKey(), entry.getValue());
  }

  public V get(K key) {
    return entries.get(key);
  }

  public V get(K key, V defaultValue) {
    return entries.getOrDefault(key, defaultValue);
  }

  public SequencedSet<K> keys() {
    return unmodifiableSequencedSet(entries.sequencedKeySet());
  }

  public void forEach(BiConsumer<? super K, ? super V> consumer) {
    entries.forEach(consumer);
  }

  public void forEachReversed(BiConsumer<? super K, ? super V> consumer) {
    entries.reversed().forEach(consumer);
  }

  public Iterable<Map.Entry<K, V>> entrySet() {
    var entrySet = entries.sequencedEntrySet();
    return () -> {
      var it = entrySet.iterator();
      return new Iterator<>() {
        @Override
        public boolean hasNext() {
          return it.hasNext();
        }

        @Override
        public Map.Entry<K, V> next() {
          var next = it.next();
          return Map.entry(next.getKey(), next.getValue());
        }
      };
    };
  }

  @Override
  public String toString() {
    return entries.toString();
  }
}
