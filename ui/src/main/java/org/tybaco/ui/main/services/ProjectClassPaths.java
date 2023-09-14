package org.tybaco.ui.main.services;

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

import jakarta.annotation.PostConstruct;
import javafx.application.Platform;
import javafx.collections.ListChangeListener.Change;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.tybaco.ui.lib.repo.ArtifactResolver;
import org.tybaco.ui.model.Lib;
import org.tybaco.ui.model.Project;

import java.util.*;

import static java.lang.Math.max;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.SEVERE;
import static org.tybaco.ui.lib.logging.Logging.LOG;

@Lazy(false)
@Component
public class ProjectClassPaths {

  private final ThreadGroup threadGroup = new ThreadGroup("classPaths");
  private final Projects projects;
  private final ArtifactResolver artifactResolver;
  private final IdentityHashMap<Project, Set<Lib>> classPaths;

  public ProjectClassPaths(Projects projects, ArtifactResolver artifactResolver) {
    this.projects = projects;
    this.artifactResolver = artifactResolver;
    this.classPaths = new IdentityHashMap<>(max(projects.projects.size(), 8));
  }

  @PostConstruct
  public void init() {
    projects.projects.forEach(this::onProject);
    projects.projects.addListener((Change<? extends Project> c) -> {
      while (c.next()) {
        if (c.wasRemoved()) {
          c.getRemoved().forEach(classPaths::remove);
        } else if (c.wasAdded()) {
          c.getAddedSubList().forEach(this::onProject);
        } else if (c.wasUpdated()) {
          for (int i = c.getFrom(); i < c.getTo(); i++) {
            var p = c.getList().get(i);
            onProject(p);
          }
        }
      }
    });
  }

  private void onProject(Project project) {
    try {
      var newLibs = Set.copyOf(project.libs);
      var oldLibs = classPaths.put(project, newLibs);
      if (newLibs.equals(oldLibs)) {
        return;
      }
      var thread = new Thread(threadGroup, () -> {
        try {
          var cp = requireNonNull(artifactResolver.resolve(project.id, newLibs));
          Platform.runLater(() -> project.classPath.set(cp));
        } catch (Throwable e) {
          LOG.log(SEVERE, "Unable to set classpath for " + project, e);
        }
      }, project.id);
      thread.setDaemon(true);
      thread.start();
    } catch (Throwable e) {
      LOG.log(SEVERE, "Unable to construct the classpath");
    }
  }
}
