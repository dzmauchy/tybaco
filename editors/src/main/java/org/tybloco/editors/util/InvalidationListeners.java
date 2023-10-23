package org.tybloco.editors.util;

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

import javafx.beans.*;

import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class InvalidationListeners implements Observable {

  private final ConcurrentLinkedQueue<InvalidationListener> listeners = new ConcurrentLinkedQueue<>();

  @Override
  public void addListener(InvalidationListener listener) {
    listeners.add(listener);
    listeners.removeIf(l -> l instanceof WeakInvalidationListener w && w.wasGarbageCollected());
  }

  @Override
  public void removeListener(InvalidationListener listener) {
    listeners.removeIf(l -> l == listener && l instanceof WeakInvalidationListener w && w.wasGarbageCollected());
  }

  protected void fire() {
    listeners.removeIf(l -> {
      if (l instanceof WeakInvalidationListener w) {
        if (w.wasGarbageCollected()) {
          return true;
        } else {
          w.invalidated(this);
          return false;
        }
      } else {
        l.invalidated(this);
        return false;
      }
    });
  }
}
