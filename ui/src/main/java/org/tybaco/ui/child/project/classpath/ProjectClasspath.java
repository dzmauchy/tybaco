package org.tybaco.ui.child.project.classpath;

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
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.tybaco.ui.lib.repo.ArtifactClassPath;
import org.tybaco.ui.lib.repo.ArtifactResolver;
import org.tybaco.ui.model.Dependency;
import org.tybaco.ui.model.Project;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.*;
import static org.tybaco.ui.lib.logging.Logging.LOG;

@Lazy(false)
@Component
public final class ProjectClasspath implements AutoCloseable {

  public final SimpleObjectProperty<ArtifactClassPath> classPath = new SimpleObjectProperty<>(this, "classPath");
  final Project project;
  private final ArtifactResolver artifactResolver;
  private final InvalidationListener libsInvalidationListener;
  private volatile Set<Dependency> libs;
  private final ConcurrentLinkedQueue<Thread> threads = new ConcurrentLinkedQueue<>();

  public ProjectClasspath(Project project, ArtifactResolver artifactResolver) {
    this.project = project;
    this.artifactResolver = artifactResolver;
    this.libsInvalidationListener = this::onChangeLibs;
    this.libs = Set.copyOf(project.dependencies);
  }

  @PostConstruct
  public void init() {
    project.dependencies.addListener(libsInvalidationListener);
  }

  private void onChangeLibs(Observable observable) {
    var newLibs = Set.copyOf(project.dependencies);
    if (newLibs.equals(libs)) {
      return;
    }
    libs = newLibs;
    var thread = new Thread(project.threadGroup, this::update, "classpath");
    threads.offer(thread);
    thread.start();
  }

  private void update() {
    try {
      var cp = requireNonNull(artifactResolver.resolve(project.id, project.dependencies));
      Platform.runLater(() -> classPath.set(cp));
    } catch (Throwable e) {
      LOG.log(WARNING, "Unable to set classpath", e);
    } finally {
      threads.remove(Thread.currentThread());
    }
  }

  @Override
  public void close() {
    threads.removeIf(thread -> {
      try {
        thread.join();
      } catch (Throwable e) {
        LOG.log(WARNING, "Unable to close the thread", e);
      }
      return true;
    });
    try (var cp = classPath.get()) {
      if (cp != null) {
        LOG.log(INFO, "Closing classpath {0}", cp.classLoader.getName());
      }
      project.dependencies.removeListener(libsInvalidationListener);
    } catch (Throwable e) {
      LOG.log(SEVERE, "Unable to close classpath " + project.id, e);
    }
  }
}
