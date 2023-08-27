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
import org.tybaco.ui.lib.id.Ids;
import org.tybaco.ui.lib.props.ListProp;
import org.tybaco.ui.lib.props.Prop;
import org.w3c.dom.Element;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.requireNonNullElse;
import static org.tybaco.model.Xml.elementsByTags;

public class Project {

  private final EventListeners eventListeners = new EventListeners();

  @Getter private final String id;
  @Getter private final Prop<String> name;
  @Getter private final ListProp<DefaultArtifact> artifacts;

  public Project(String name) {
    this(Ids.newId(), name, List.of());
  }

  private Project(String id, String name, Collection<DefaultArtifact> artifacts) {
    this.id = id;
    this.name = new Prop<>(this, "name", eventListeners, name);
    this.artifacts = new ListProp<>(this, "artifacts", eventListeners, artifacts);
  }

  public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    eventListeners.add(PropertyChangeListener.class, propertyChangeListener);
  }

  public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    eventListeners.remove(PropertyChangeListener.class, propertyChangeListener);
  }

  public static Project loadFrom(Element element) {
    return new Project(
      element.getAttribute("id"),
      element.getAttribute("name"),
      elementsByTags(element, "artifacts", "artifact").map(Project::artifactFrom).toList()
    );
  }

  private static DefaultArtifact artifactFrom(Element element) {
    return new DefaultArtifact(
      element.getAttribute("groupId"),
      element.getAttribute("artifactId"),
      requireNonNullElse(element.getAttribute("classifier"), ""),
      requireNonNullElse(element.getAttribute("extension"), "jar"),
      element.getAttribute("version")
    );
  }
}
