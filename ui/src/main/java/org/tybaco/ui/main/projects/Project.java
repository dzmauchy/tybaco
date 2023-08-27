package org.tybaco.ui.main.projects;

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

import lombok.Getter;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.tybaco.ui.lib.event.EventListeners;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class Project {

  private final EventListeners eventListeners = new EventListeners();
  private final ArrayList<DefaultArtifact> dependencies = new ArrayList<>();

  @Getter private String name;

  public Project(String name) {
    this.name = name;
  }

  public void setName(String name) {
    if (!name.equals(this.name)) {
      var event = new PropertyChangeEvent(this, "name", this.name, name);
      var changeEvent = new ChangeEvent(this);
      this.name = name;
      eventListeners.fireListeners(PropertyChangeListener.class, l -> l.propertyChange(event));
      eventListeners.fireListeners(ChangeListener.class, l -> l.stateChanged(changeEvent));
    }
  }

  public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    eventListeners.add(PropertyChangeListener.class, propertyChangeListener);
  }

  public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    eventListeners.remove(PropertyChangeListener.class, propertyChangeListener);
  }
}
