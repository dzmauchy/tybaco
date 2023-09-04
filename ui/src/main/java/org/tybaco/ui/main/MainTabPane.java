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

import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.tybaco.ui.child.project.ProjectBean;
import org.tybaco.ui.child.project.ProjectPane;
import org.tybaco.ui.lib.context.ChildContext;
import org.tybaco.ui.lib.context.UIComponent;
import org.tybaco.ui.lib.icon.Icons;
import org.tybaco.ui.main.services.Projects;
import org.tybaco.ui.model.Project;

@UIComponent
public class MainTabPane extends TabPane {

  private final AnnotationConfigApplicationContext context;

  public MainTabPane(Projects projects, AnnotationConfigApplicationContext context) {
    this.context = context;
    projects.projects.addListener((Change<? extends Project> c) -> {
      while (c.next()) {
        if (c.wasAdded()) {
          c.getAddedSubList().forEach(this::onAddProject);
        } else if (c.wasRemoved()) {
          c.getRemoved().forEach(this::onRemoveProject);
        }
      }
    });
  }

  private void onAddProject(Project project) {
    var tab = getTabs().stream()
      .filter(t -> project.id.equals(t.getId()))
      .findFirst()
      .orElseGet(() -> {
        var child = new ChildContext(project.id, project.name.get(), context);
        child.registerBean(ProjectBean.class, project);
        child.register(ProjectPane.class);
        var nameListener = (ChangeListener<String>) (o, oldValue, newValue) -> child.setDisplayName(project.name.get());
        project.name.addListener(nameListener);
        child.addApplicationListener(event -> {
          if (event instanceof ContextClosedEvent) {
            project.name.removeListener(nameListener);
          }
        });
        return child.refreshAndStart(c -> {
          var p = c.getBean(ProjectPane.class);
          var t = new Tab(project.name.get(), p);
          t.setGraphic(Icons.icon(MaterialDesignP.PACKAGE_VARIANT, 24));
          t.setOnCloseRequest(e -> child.stop());
          t.setOnClosed(e -> child.close());
          p.setId(project.id);
          getTabs().add(t);
          return t;
        });
      });
    getSelectionModel().select(tab);
  }

  private void onRemoveProject(Project project) {
    getTabs().removeIf(t -> project.id.equals(t.getId()));
  }
}
