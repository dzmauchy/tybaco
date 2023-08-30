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

import javax.swing.event.ChangeListener;
import java.beans.PropertyChangeListener;

public abstract class AbstractEntity {

  protected final UniEventListeners<PropertyChangeListener> propertyChangeListeners = new UniEventListeners<>();
  protected final UniEventListeners<ChangeListener> changeListeners = new UniEventListeners<>();

  public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    propertyChangeListeners.add(propertyChangeListener);
  }

  public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    propertyChangeListeners.remove(propertyChangeListener);
  }

  public void addChangeListener(ChangeListener changeListener) {
    changeListeners.add(changeListener);
  }

  public void removeChangeListener(ChangeListener changeListener) {
    changeListeners.remove(changeListener);
  }
}
