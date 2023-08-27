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

import org.tybaco.ui.lib.event.UniEventListeners;

import javax.swing.*;
import javax.swing.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import static javax.swing.event.ListDataEvent.*;

public final class ListProp<E> implements ListModel<E> {

  private final UniEventListeners<ListDataListener> eventListeners = new UniEventListeners<>();
  private final AbstractEntity source;
  private final String name;
  private final ArrayList<E> elements;

  public ListProp(AbstractEntity source, String name, Collection<E> elements) {
    this.source = source;
    this.name = name;
    this.elements = new ArrayList<>(elements);
  }

  public ListProp(AbstractEntity source, String name) {
    this(source, name, List.of());
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
    if (elements.isEmpty()) {
      return;
    }
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
    var list = elements.subList(from, to);
    var result = List.copyOf(list);
    list.clear();
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
  }

  public void add(int index, E element) {
    elements.add(index, element);
    var event = new ListDataEvent(source, INTERVAL_ADDED, index, index);
    eventListeners.forEach(l -> l.intervalAdded(event));
    fireParentEvents();
  }

  public void addAll(Collection<? extends E> collection) {
    if (collection.isEmpty()) {
      return;
    }
    var event = new ListDataEvent(source, INTERVAL_ADDED, elements.size(), elements.size() + collection.size() - 1);
    elements.addAll(collection);
    eventListeners.forEach(l -> l.intervalAdded(event));
    fireParentEvents();
  }

  public void addAll(int index, Collection<? extends E> collection) {
    if (collection.isEmpty()) {
      return;
    }
    elements.addAll(index, collection);
    var event = new ListDataEvent(source, INTERVAL_ADDED, index, index + collection.size() - 1);
    eventListeners.forEach(l -> l.intervalAdded(event));
    fireParentEvents();
  }

  public void set(int index, E element) {
    elements.set(index, element);
    var event = new ListDataEvent(source, CONTENTS_CHANGED, index, index);
    eventListeners.forEach(l -> l.contentsChanged(event));
    fireParentEvents();
  }

  public void setAll(int index, List<E> list) {
    int listSize = list.size();
    for (int i = listSize - 1; i >= 0; i--) {
      elements.set(i + index, list.get(i));
    }
    var event = new ListDataEvent(source, CONTENTS_CHANGED, index, index + listSize - 1);
    eventListeners.forEach(l -> l.contentsChanged(event));
    fireParentEvents();
  }

  public List<E> removeIndices(int... indices) {
    if (indices.length == 0) {
      return List.of();
    }
    var result = new ArrayList<E>(indices.length);
    Arrays.sort(indices);
    for (int i = indices.length - 1; i >= 0; i--) {
      int index = indices[i];
      result.add(elements.remove(index));
      var event = new ListDataEvent(source, INTERVAL_REMOVED, index, index);
      eventListeners.forEach(l -> l.intervalRemoved(event));
    }
    fireParentEvents();
    return result;
  }
}
