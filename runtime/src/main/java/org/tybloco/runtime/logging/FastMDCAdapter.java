package org.tybloco.runtime.logging;

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

import org.slf4j.spi.MDCAdapter;

import java.util.*;

public class FastMDCAdapter implements MDCAdapter {

  final InheritableThreadLocal<TreeMap<String, String>> map = new InheritableThreadLocal<>() {
    @Override
    protected TreeMap<String, String> initialValue() {
      return new TreeMap<>();
    }

    @Override
    protected TreeMap<String, String> childValue(TreeMap<String, String> parentValue) {
      return parentValue == null ? new TreeMap<>() : new TreeMap<>(parentValue);
    }
  };
  final ThreadLocal<TreeMap<String, LinkedList<String>>> queues = ThreadLocal.withInitial(TreeMap::new);

  @Override
  public void put(String key, String val) {
    map.get().put(key, val);
  }

  @Override
  public String get(String key) {
    return map.get().get(key);
  }

  @Override
  public void remove(String key) {
    map.get().remove(key);
  }

  @Override
  public void clear() {
    map.get().clear();
  }

  @Override
  public Map<String, String> getCopyOfContextMap() {
    return new TreeMap<>(map.get());
  }

  @Override
  public void setContextMap(Map<String, String> contextMap) {
    var m = map.get();
    m.clear();
    if (contextMap != null) {
      m.putAll(contextMap);
    }
  }

  @Override
  public void pushByKey(String key, String value) {
    queues.get()
      .computeIfAbsent(key, k -> new LinkedList<>())
      .addFirst(value);
  }

  @Override
  public String popByKey(String key) {
    var m = queues.get();
    var q = m.get(key);
    if (q == null) return null;
    var e = q.pollFirst();
    if (e == null) {
      m.remove(key);
      return null;
    } else {
      return e;
    }
  }

  @Override
  public Deque<String> getCopyOfDequeByKey(String key) {
    var m = queues.get();
    var q = m.get(key);
    return q == null ? null : new LinkedList<>(q);
  }

  @Override
  public void clearDequeByKey(String key) {
    queues.get().remove(key);
  }
}
