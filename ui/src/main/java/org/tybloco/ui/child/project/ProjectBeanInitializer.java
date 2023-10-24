package org.tybloco.ui.child.project;

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

import javafx.application.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.tybloco.ui.main.project.Projects;
import org.tybloco.ui.model.Dependency;
import org.tybloco.ui.model.Project;

import static org.tybloco.logging.Log.info;

@Lazy(false)
@Component
public final class ProjectBeanInitializer {

  private final Project project;
  private final Projects projects;

  public ProjectBeanInitializer(Project project, Projects projects) {
    this.project = project;
    this.projects = projects;
  }

  @Autowired
  public void init(Environment environment) {
    var version = environment.getProperty("ui.version");
    info(getClass(), "Init dependencies for {0}", version);
    Platform.runLater(() -> {
      project.dependencies.removeIf(d -> "org.montoni".equals(d.group()) && "tybloco-runtime".equals(d.artifact()));
      project.dependencies.add(new Dependency("org.montoni", "tybloco-runtime", version));
    });
  }

  @EventListener
  public void onClose(ContextClosedEvent event) {
    projects.projects.remove(project);
  }
}
