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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

public class Prop<T> {

  private final AbstractEntity source;
  private final String name;

  private T value;

  public Prop(AbstractEntity source, String name, T value) {
    this.source = source;
    this.name = name;
    this.value = value;
  }

  public void set(T value) {
    if (!Objects.equals(this.value, value)) {
      var event = new PropertyChangeEvent(source, name, this.value, value);
      var changeEvent = new ChangeEvent(source);
      this.value = value;
      source.eventListeners.fireListeners(PropertyChangeListener.class, l -> l.propertyChange(event));
      source.eventListeners.fireListeners(ChangeListener.class, l -> l.stateChanged(changeEvent));
    }
  }

  public T get() {
    return value;
  }

  public String getName() {
    return name;
  }
}
