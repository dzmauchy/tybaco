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

import org.tybaco.ui.lib.event.EventListeners;

import javax.swing.event.ChangeListener;
import java.beans.PropertyChangeListener;

public abstract class AbstractEntity {

  protected final EventListeners eventListeners = new EventListeners();

  public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    eventListeners.add(PropertyChangeListener.class, propertyChangeListener);
  }

  public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    eventListeners.remove(PropertyChangeListener.class, propertyChangeListener);
  }

  public void addChangeListener(ChangeListener changeListener) {
    eventListeners.add(ChangeListener.class, changeListener);
  }

  public void removeChangeListener(ChangeListener changeListener) {
    eventListeners.remove(ChangeListener.class, changeListener);
  }
}
