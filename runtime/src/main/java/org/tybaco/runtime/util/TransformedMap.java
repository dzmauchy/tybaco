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

import java.util.*;
import java.util.function.BiFunction;

@SuppressWarnings("NullableProblems")
public final class TransformedMap<K, V, R> implements Map<K, R> {

  private final SortedMap<K, V> delegate;
  private final BiFunction<K, V, R> transform;

  public TransformedMap(SortedMap<K, V> delegate, BiFunction<K, V, R> transform) {
    this.delegate = delegate;
    this.transform = transform;
  }

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return delegate.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unchecked")
  @Override
  public R get(Object key) {
    return transform.apply((K) key, delegate.get(key));
  }

  @Override
  public R put(K key, R value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public R remove(Object key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void putAll(Map<? extends K, ? extends R> m) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<K> keySet() {
    return delegate.keySet();
  }

  @Override
  public Collection<R> values() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<Entry<K, R>> entrySet() {
    throw new UnsupportedOperationException();
  }
}
