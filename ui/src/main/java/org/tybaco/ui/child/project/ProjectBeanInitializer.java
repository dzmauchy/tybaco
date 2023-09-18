package org.tybaco.ui.child.project;

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
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.tybaco.ui.model.Dependency;
import org.tybaco.ui.model.Project;

import java.util.ArrayList;

import static java.util.logging.Level.INFO;
import static org.tybaco.ui.lib.logging.Logging.LOG;

@Lazy(false)
@Component
public final class ProjectBeanInitializer {

  private final Project project;

  public ProjectBeanInitializer(Project project) {
    this.project = project;
  }

  @Autowired
  public void init(Environment environment) {
    var version = environment.getProperty("ui.version");
    LOG.log(INFO, "Init dependencies for {0}", version);
    Platform.runLater(() -> {
      project.dependencies.removeIf(d -> "org.montoni".equals(d.group()) && "tybaco-runtime".equals(d.artifact()));
      project.dependencies.add(new Dependency("org.montoni", "tybaco-runtime", version));
    });
  }
}
