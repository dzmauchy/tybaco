package org.tybaco.ui.main;

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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.tybaco.ui.child.logging.LogFrame;
import org.tybaco.ui.child.project.ProjectPane;
import org.tybaco.ui.lib.actions.SmartAction;
import org.tybaco.ui.main.projects.Project;

import static org.tybaco.ui.lib.context.ChildContext.child;
import static org.tybaco.ui.lib.dialogs.Dialogs.input;
import static org.tybaco.ui.lib.window.Windows.findWindow;

@ComponentScan(lazyInit = true)
@Configuration(proxyBeanMethods = false)
public class MainConfiguration {

  @Bean
  @Qualifier("log")
  public SmartAction showLogFrameAction(AnnotationConfigApplicationContext context) {
    return new SmartAction("showLogs", "Show logs", "icon/logs.svg", e -> {
      var frame = findWindow(LogFrame.class).orElseGet(() -> child("logs", "Logs", LogFrame.class, context));
      frame.setVisible(true);
      frame.toFront();
    }).group("|");
  }

  @Bean
  @Qualifier("file")
  public SmartAction newProjectAction(MainTabPane tabPane) {
    return new SmartAction("newProject", "New project", "icon/project.svg", e -> {
      var defName = tabPane.guessNewProjectName();
      input(tabPane, "Project name", "New project", "icon/project.svg", defName, name -> {
        var project = new Project(name);
        tabPane.tab(project.id, project.getName(), ProjectPane.class, c -> c.registerBean(Project.class, () -> project));
      });
    }).group("a");
  }
}
