package org.tybaco.ui.lib.event;

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

import javax.swing.event.EventListenerList;
import java.util.EventListener;
import java.util.function.Consumer;

public final class EventListeners extends EventListenerList {

  public <L extends EventListener> void fireListeners(Class<L> type, Consumer<L> consumer) {
    var list = listenerList;
    for (int i = list.length - 2; i >= 0; i -= 2) {
      if (list[i] == type) {
        consumer.accept(type.cast(list[i + 1]));
      }
    }
  }
}
