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

import jakarta.annotation.PreDestroy;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import org.springframework.stereotype.Component;
import org.tybaco.editors.util.InvalidationListeners;
import org.tybaco.ui.lib.repo.ArtifactClassPath;
import org.tybaco.ui.lib.repo.ArtifactResolver;
import org.tybaco.ui.model.Dependency;
import org.tybaco.ui.model.Project;

import java.util.Set;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static java.lang.Thread.ofVirtual;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.locks.LockSupport.parkNanos;
import static org.tybaco.logging.Log.error;
import static org.tybaco.logging.Log.warn;

@Component
public final class ProjectClasspath extends InvalidationListeners {

  private final SimpleObjectProperty<ArtifactClassPath> classPath = new SimpleObjectProperty<>(this, "classPath");
  private final Project project;
  private final ArtifactResolver artifactResolver;
  private final InvalidationListener libsInvalidationListener = this::onChangeLibs;
  private final ThreadPoolExecutor threads;

  private volatile ArtifactClassPath currentClassPath;
  private volatile Set<Dependency> deps;

  public ProjectClasspath(Project project, ArtifactResolver artifactResolver) {
    this.project = project;
    this.artifactResolver = artifactResolver;
    this.threads = new ThreadPoolExecutor(1, 1, 1L, MINUTES, new LinkedTransferQueue<>(), ofVirtual()::unstarted);
    this.threads.allowCoreThreadTimeOut(true);
    this.classPath.addListener(o -> fire());
    this.project.dependencies.addListener(libsInvalidationListener);
  }

  private void onChangeLibs(Observable o) {
    threads.execute(() -> {
      parkNanos(MILLISECONDS.toNanos(100L));
      Platform.runLater(() -> {
        var newDeps = Set.copyOf(project.dependencies);
        if (!newDeps.equals(deps)) {
          deps = newDeps;
          threads.execute(this::update);
        }
      });
    });
  }

  private void update() {
    try {
      var cp = requireNonNull(artifactResolver.resolve(project.id, deps));
      var oldClassPath = currentClassPath;
      currentClassPath = cp;
      if (oldClassPath != null) {
        oldClassPath.close();
      }
      Platform.runLater(() -> classPath.set(cp));
    } catch (Throwable e) {
      warn(getClass(), "Unable to set classpath", e);
    }
  }

  public ClassLoader getClassLoader() {
    var cp = classPath.get();
    return cp == null ? Thread.currentThread().getContextClassLoader() : cp.classLoader;
  }

  @PreDestroy
  private void close() {
    project.dependencies.removeListener(libsInvalidationListener);
    try (threads) {
      warn(getClass(), "Closing threads {0}", threads.getActiveCount());
    }
    try (var cp = currentClassPath) {
      if (cp != null) {
        warn(getClass(), "Closing classpath {0}", cp.classLoader.getName());
      }
    } catch (Throwable e) {
      error(getClass(), "Unable to close classpath " + project.id, e);
    }
  }
}
