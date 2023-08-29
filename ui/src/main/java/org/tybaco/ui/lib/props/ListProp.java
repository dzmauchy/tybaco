package org.tybaco.ui.lib.props;

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

import org.apache.commons.lang3.ArrayUtils;
import org.tybaco.ui.lib.event.UniEventListeners;

import javax.swing.*;
import javax.swing.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.function.Consumer;

import static java.util.Arrays.copyOf;
import static java.util.Arrays.copyOfRange;
import static javax.swing.event.ListDataEvent.*;
import static org.apache.commons.lang3.ArrayUtils.EMPTY_OBJECT_ARRAY;

public final class ListProp<E> implements ListModel<E> {

  private final UniEventListeners<ListDataListener> eventListeners = new UniEventListeners<>();
  private final AbstractEntity source;
  private final String name;
  private final InternalList<E> elements;

  public ListProp(AbstractEntity source, String name, Collection<E> elements) {
    this.source = source;
    this.name = name;
    this.elements = new InternalList<>(elements);
  }

  public ListProp(AbstractEntity source, String name) {
    this(source, name, List.of());
  }

  public void forEach(Consumer<E> consumer) {
    elements.forEach(consumer);
  }

  @Override
  public int getSize() {
    return elements.size();
  }

  @Override
  public E getElementAt(int index) {
    return elements.get(index);
  }

  @Override
  public void addListDataListener(ListDataListener l) {
    eventListeners.add(l);
  }

  @Override
  public void removeListDataListener(ListDataListener l) {
    eventListeners.remove(l);
  }

  private void fireParentEvents() {
    var propertyChangeEvent = new PropertyChangeEvent(source, name, this, this);
    source.eventListeners.fireListeners(PropertyChangeListener.class, l -> l.propertyChange(propertyChangeEvent));
    var changeEvent = new ChangeEvent(source);
    source.eventListeners.fireListeners(ChangeListener.class, l -> l.stateChanged(changeEvent));
  }

  public void clear() {
    if (elements.isEmpty()) return;
    var event = new ListDataEvent(source, INTERVAL_REMOVED, 0, elements.size() - 1);
    elements.clear();
    eventListeners.forEach(l -> l.intervalRemoved(event));
    fireParentEvents();
  }

  public E remove(int index) {
    var element = elements.remove(index);
    var event = new ListDataEvent(source, INTERVAL_REMOVED, index, index);
    eventListeners.forEach(l -> l.intervalRemoved(event));
    fireParentEvents();
    return element;
  }

  public List<E> remove(int from, int to) {
    var result = elements.remove(from, to);
    var event = new ListDataEvent(source, INTERVAL_REMOVED, from, to - 1);
    eventListeners.forEach(l -> l.intervalRemoved(event));
    fireParentEvents();
    return result;
  }

  public void add(E element) {
    var event = new ListDataEvent(source, INTERVAL_ADDED, elements.size(), elements.size());
    elements.add(element);
    eventListeners.forEach(l -> l.intervalAdded(event));
    fireParentEvents();
    postProcessNewElement(element);
  }

  public void add(int index, E element) {
    elements.add(index, element);
    var event = new ListDataEvent(source, INTERVAL_ADDED, index, index);
    eventListeners.forEach(l -> l.intervalAdded(event));
    fireParentEvents();
    postProcessNewElement(element);
  }

  public void addAll(Collection<? extends E> collection) {
    if (collection.isEmpty()) return;
    var event = new ListDataEvent(source, INTERVAL_ADDED, elements.size(), elements.size() + collection.size() - 1);
    elements.addAll(collection);
    eventListeners.forEach(l -> l.intervalAdded(event));
    fireParentEvents();
    collection.forEach(this::postProcessNewElement);
  }

  public void addAll(int index, Collection<? extends E> collection) {
    if (collection.isEmpty()) return;
    elements.addAll(index, collection);
    var event = new ListDataEvent(source, INTERVAL_ADDED, index, index + collection.size() - 1);
    eventListeners.forEach(l -> l.intervalAdded(event));
    fireParentEvents();
    collection.forEach(this::postProcessNewElement);
  }

  public void set(int index, E element) {
    elements.set(index, element);
    var event = new ListDataEvent(source, CONTENTS_CHANGED, index, index);
    eventListeners.forEach(l -> l.contentsChanged(event));
    fireParentEvents();
    postProcessNewElement(element);
  }

  public void setAll(int index, List<E> list) {
    int listSize = list.size();
    for (int i = listSize - 1; i >= 0; i--) {
      elements.set(i + index, list.get(i));
    }
    var event = new ListDataEvent(source, CONTENTS_CHANGED, index, index + listSize - 1);
    eventListeners.forEach(l -> l.contentsChanged(event));
    fireParentEvents();
    list.forEach(this::postProcessNewElement);
  }

  public List<E> removeAll(int... indices) {
    var result = elements.removeAll(indices);
    Arrays.sort(indices);
    for (int i = indices.length - 1; i >= 0; i--) {
      int index = indices[i];
      var event = new ListDataEvent(source, INTERVAL_REMOVED, index, index);
      eventListeners.forEach(l -> l.intervalRemoved(event));
    }
    fireParentEvents();
    return result;
  }

  public int find(E element) {
    return find0(element);
  }

  private int find0(Object element) {
    int size = elements.size();
    for (int i = 0; i < size; i++) {
      var e = elements.get(i);
      if (e == element) {
        return i;
      }
    }
    return -1;
  }

  private void onChangeChild(ChangeEvent event) {
    var i = find0(event.getSource());
    if (i < 0) return;
    var listDataEvent = new ListDataEvent(source, CONTENTS_CHANGED, i, i);
    eventListeners.forEach(l -> l.contentsChanged(listDataEvent));
    fireParentEvents();
  }

  private void postProcessNewElement(Object element) {
    if (element instanceof AbstractEntity e) {
      e.addChangeListener(this::onChangeChild);
    }
  }

  @SuppressWarnings("unchecked")
  private static final class InternalList<E> {

    private Object[] data;

    private InternalList(Collection<? extends E> collection) {
      data = collection.isEmpty() ? EMPTY_OBJECT_ARRAY : collection.toArray();
    }

    private void forEach(Consumer<E> consumer) {
      for (var e : data) {
        consumer.accept((E) e);
      }
    }

    private E get(int index) {
      return (E) data[index];
    }

    private int size() {
      return data.length;
    }

    private boolean isEmpty() {
      return data.length == 0;
    }

    private void clear() {
      data = EMPTY_OBJECT_ARRAY;
    }

    private List<E> remove(int from, int to) {
      var slice = copyOfRange(data, from, to, Object[].class);
      if (from == 0) {
        data = copyOfRange(data, to, data.length, Object[].class);
      } else if (to == data.length) {
        data = copyOf(data, from, Object[].class);
      } else {
        data = ArrayUtils.addAll(
          copyOf(data, from, Object[].class),
          copyOfRange(data, to, data.length, Object[].class)
        );
      }
      return (List<E>) List.of(slice);
    }

    private E remove(int index) {
      var e = data[index];
      data = ArrayUtils.remove(data, index);
      return (E) e;
    }

    private void add(E element) {
      data = ArrayUtils.add(data, element);
    }

    private void addAll(Collection<? extends E> elements) {
      data = ArrayUtils.addAll(data, elements.toArray());
    }

    private void add(int i, E element) {
      data = ArrayUtils.insert(i, data, element);
    }

    private void addAll(int i, Collection<? extends E> elements) {
      data = ArrayUtils.insert(i, elements.toArray());
    }

    private void set(int i, E element) {
      data[i] = element;
    }

    private List<E> removeAll(int[] indices) {
      var result = new Object[indices.length];
      for (int i = indices.length - 1; i >= 0; i--) {
        result[i] = data[indices[i]];
      }
      data = ArrayUtils.removeAll(data, indices);
      return (List<E>) List.of(result);
    }
  }
}
