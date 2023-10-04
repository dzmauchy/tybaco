package org.tybaco.runtime.logging;

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

import org.slf4j.IMarkerFactory;
import org.slf4j.Marker;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class FastMarkerFactory implements IMarkerFactory {

  private final ConcurrentHashMap<String, FastMarker> map = new ConcurrentHashMap<>(16, 0.5f);

  @Override
  public Marker getMarker(String name) {
    return map.computeIfAbsent(name, FastMarker::new);
  }

  @Override
  public boolean exists(String name) {
    return map.containsKey(name);
  }

  @Override
  public boolean detachMarker(String name) {
    return map.remove(name) != null;
  }

  @Override
  public Marker getDetachedMarker(String name) {
    return new FastMarker(name);
  }

  private static final class FastMarker implements Marker {

    private final String name;
    private final ConcurrentLinkedQueue<Marker> children = new ConcurrentLinkedQueue<>();

    private FastMarker(String name) {
      this.name = name;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public void add(Marker reference) {
      if (reference == null) throw new IllegalArgumentException("Marker reference cannot be null");
      if (!contains(reference) && !reference.contains(this)) children.offer(reference);
    }

    @Override
    public boolean remove(Marker reference) {
      return children.remove(reference);
    }

    @Override
    public boolean hasChildren() {
      return !children.isEmpty();
    }

    @Override
    public boolean hasReferences() {
      return !children.isEmpty();
    }

    @Override
    public Iterator<Marker> iterator() {
      return children.iterator();
    }

    @Override
    public boolean contains(Marker other) {
      if (other == null) throw new IllegalArgumentException("Marker reference cannot be null");
      if (equals(other)) return true;
      for (var ref : children) {
        if (ref.contains(other)) return true;
      }
      return false;
    }

    @Override
    public boolean contains(String name) {
      if (name == null) throw new IllegalArgumentException("Marker name cannot be null");
      if (this.name.equals(name)) return true;
      for (var ref : children) {
        if (ref.contains(name)) return true;
      }
      return false;
    }

    @Override
    public int hashCode() {
      return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof Marker m && name.equals(m.getName());
    }

    @Override
    public String toString() {
      var b = new StringBuilder("{").append(name);
      children.forEach(c -> visit(b, c));
      return b.append('}').toString();
    }

    private static void visit(StringBuilder b, Marker marker) {
      b.append(',').append(marker.getName());
      for (var it = marker.iterator(); it.hasNext(); ) {
        visit(b, it.next());
      }
    }
  }
}
