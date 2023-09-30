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
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleObjectProperty;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.tybaco.editors.model.ConstLib;
import org.tybaco.ui.lib.repo.ArtifactClassPath;
import org.tybaco.ui.lib.repo.ArtifactResolver;
import org.tybaco.ui.model.Dependency;
import org.tybaco.ui.model.Project;

import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.tybaco.logging.Log.*;

@Component
public final class ProjectClasspath implements AutoCloseable {

  public final SimpleObjectProperty<ArtifactClassPath> classPath = new SimpleObjectProperty<>(this, "classPath");
  public final SimpleObjectProperty<List<ConstLib>> constLibs = new SimpleObjectProperty<>(this, "constLibs", List.of());
  public final BooleanBinding classPathIsNotSet = classPath.isNull();
  private final Project project;
  private final ArtifactResolver artifactResolver;
  private final InvalidationListener libsInvalidationListener;
  private final ThreadPoolExecutor threads;

  private Set<Dependency> libs;
  private volatile ArtifactClassPath currentClassPath;

  public ProjectClasspath(Project project, ArtifactResolver artifactResolver) {
    this.project = project;
    this.artifactResolver = artifactResolver;
    this.libsInvalidationListener = this::onChangeLibs;
    this.libs = Set.copyOf(project.dependencies);
    this.threads = new ThreadPoolExecutor(1, 1, 1L, MINUTES, new LinkedTransferQueue<>(), Thread.ofVirtual()::unstarted);
    this.threads.allowCoreThreadTimeOut(true);
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
    try (var old = classPath.get()) {
      if (old != null) {
        info(getClass(), "Closing old classpath");
      }
      classPath.set(null);
    } catch (Throwable e) {
      warn(getClass(), "Unable to close the old classpath");
    }
    threads.execute(this::update);
  }

  private void update() {
    try {
      var cp = requireNonNull(artifactResolver.resolve(project.id, project.dependencies));
      currentClassPath = cp;
      try (var ctx = new GenericXmlApplicationContext("classpath*:tybaco/editors/config.xml")) {
        var provider = ctx.getBeanProvider(ConstLib.class);
        var list = provider.stream().toList();
        Platform.runLater(() -> constLibs.set(list));
      }
      Platform.runLater(() -> classPath.set(cp));
    } catch (Throwable e) {
      warn(getClass(), "Unable to set classpath", e);
    }
  }

  @Override
  public void close() {
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
