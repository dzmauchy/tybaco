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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;

import static java.util.Arrays.stream;
import static java.util.Map.entry;
import static java.util.Spliterator.*;
import static java.util.stream.Collectors.joining;

public final class SeqMap<K, V> implements Iterable<Entry<@NotNull K, @NotNull V>> {

  private final Entry<@NotNull K, @NotNull V>[] entries;

  @SafeVarargs
  public SeqMap(@NotNull Entry<@NotNull K, @NotNull V>... entries) {
    this.entries = entries;
  }

  public SeqMap(@NotNull K k1, @NotNull V v1) {
    this(entry(k1, v1));
  }

  public SeqMap(@NotNull K k1, @NotNull V v1, @NotNull K k2, @NotNull V v2) {
    this(entry(k1, v1), entry(k2, v2));
  }

  public SeqMap(@NotNull K k1, @NotNull V v1, @NotNull K k2, @NotNull V v2, @NotNull K k3, @NotNull V v3) {
    this(entry(k1, v1), entry(k2, v2), entry(k3, v3));
  }

  public SeqMap(@NotNull K k1, @NotNull V v1, @NotNull K k2, @NotNull V v2, @NotNull K k3, @NotNull V v3, @NotNull K k4, @NotNull V v4) {
    this(entry(k1, v1), entry(k2, v2), entry(k3, v3), entry(k4, v4));
  }

  public SeqMap(@NotNull K k1, @NotNull V v1, @NotNull K k2, @NotNull V v2, @NotNull K k3, @NotNull V v3, @NotNull K k4, @NotNull V v4, @NotNull K k5, @NotNull V v5) {
    this(entry(k1, v1), entry(k2, v2), entry(k3, v3), entry(k4, v4), entry(k5, v5));
  }

  public SeqMap(@NotNull K k1, @NotNull V v1, @NotNull K k2, @NotNull V v2, @NotNull K k3, @NotNull V v3, @NotNull K k4, @NotNull V v4, @NotNull K k5, @NotNull V v5, @NotNull K k6, @NotNull V v6) {
    this(entry(k1, v1), entry(k2, v2), entry(k3, v3), entry(k4, v4), entry(k5, v5), entry(k6, v6));
  }

  public SeqMap(@NotNull K k1, @NotNull V v1, @NotNull K k2, @NotNull V v2, @NotNull K k3, @NotNull V v3, @NotNull K k4, @NotNull V v4, @NotNull K k5, @NotNull V v5, @NotNull K k6, @NotNull V v6, @NotNull K k7, @NotNull V v7) {
    this(entry(k1, v1), entry(k2, v2), entry(k3, v3), entry(k4, v4), entry(k5, v5), entry(k6, v6), entry(k7, v7));
  }

  public SeqMap(@NotNull K k1, @NotNull V v1, @NotNull K k2, @NotNull V v2, @NotNull K k3, @NotNull V v3, @NotNull K k4, @NotNull V v4, @NotNull K k5, @NotNull V v5, @NotNull K k6, @NotNull V v6, @NotNull K k7, @NotNull V v7, @NotNull K k8, @NotNull V v8) {
    this(entry(k1, v1), entry(k2, v2), entry(k3, v3), entry(k4, v4), entry(k5, v5), entry(k6, v6), entry(k7, v7), entry(k8, v8));
  }

  public SeqMap(@NotNull K k1, @NotNull V v1, @NotNull K k2, @NotNull V v2, @NotNull K k3, @NotNull V v3, @NotNull K k4, @NotNull V v4, @NotNull K k5, @NotNull V v5, @NotNull K k6, @NotNull V v6, @NotNull K k7, @NotNull V v7, @NotNull K k8, @NotNull V v8, @NotNull K k9, @NotNull V v9) {
    this(entry(k1, v1), entry(k2, v2), entry(k3, v3), entry(k4, v4), entry(k5, v5), entry(k6, v6), entry(k7, v7), entry(k8, v8), entry(k9, v9));
  }

  public SeqMap(@NotNull K k1, @NotNull V v1, @NotNull K k2, @NotNull V v2, @NotNull K k3, @NotNull V v3, @NotNull K k4, @NotNull V v4, @NotNull K k5, @NotNull V v5, @NotNull K k6, @NotNull V v6, @NotNull K k7, @NotNull V v7, @NotNull K k8, @NotNull V v8, @NotNull K k9, @NotNull V v9, @NotNull K k10, @NotNull V v10) {
    this(entry(k1, v1), entry(k2, v2), entry(k3, v3), entry(k4, v4), entry(k5, v5), entry(k6, v6), entry(k7, v7), entry(k8, v8), entry(k9, v9), entry(k10, v10));
  }

  @Nullable
  public V get(@NotNull K key) {
    return get(key, null);
  }

  @Nullable
  public V get(@NotNull K key, @Nullable V defaultValue) {
    for (var e : entries) {
      if (key.equals(e.getKey())) {
        return e.getValue();
      }
    }
    return defaultValue;
  }

  @NotNull
  public <E extends Throwable> V getOrElseThrow(@NotNull K key, @NotNull Supplier<@NotNull E> thrown) throws E {
    var v = get(key);
    if (v == null) throw thrown.get();
    else return v;
  }

  @Nullable
  public V getOrElseGet(@NotNull K key, @NotNull Supplier<@Nullable V> defaultValue) {
    var v = get(key);
    return v == null ? defaultValue.get() : v;
  }

  public void forEach(BiConsumer<? super @NotNull K, ? super @NotNull V> consumer) {
    for (var e : entries) {
      consumer.accept(e.getKey(), e.getValue());
    }
  }

  @Override
  public void forEach(@NotNull Consumer<? super Entry<@NotNull K, @NotNull V>> action) {
    for (var e : entries) {
      action.accept(e);
    }
  }

  @NotNull
  @Override
  public Iterator<@NotNull Entry<@NotNull K, @NotNull V>> iterator() {
    return new Iterator<>() {

      private int i = -1;

      @Override
      public boolean hasNext() {
        return i + 1 < entries.length;
      }

      @NotNull
      @Override
      public Entry<@NotNull K, @NotNull V> next() {
        return entries[++i];
      }
    };
  }

  @NotNull
  @Override
  public Spliterator<@NotNull Entry<@NotNull K, @NotNull V>> spliterator() {
    return Spliterators.spliterator(entries, DISTINCT | IMMUTABLE | NONNULL);
  }

  @NotNull
  @Override
  public String toString() {
    return stream(entries).map(Entry::toString).collect(joining(",", "{", "}"));
  }
}
