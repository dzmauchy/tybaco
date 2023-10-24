package org.tybloco.ui.main.actions;

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

import org.kordamp.ikonli.ionicons4.Ionicons4IOS;
import org.kordamp.ikonli.materialdesign2.*;
import org.kordamp.ikonli.unicons.UniconsLine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.tybloco.editors.action.Action;
import org.tybloco.ui.main.project.ProjectLoadDialog;
import org.tybloco.ui.main.project.Projects;

@Component
public class ProjectActions {

  @Order(1)
  @Bean
  @Qualifier("projectMenu")
  public Action projectNewAction(Projects projects) {
    return new Action("New project", MaterialDesignP.PACKAGE_VARIANT, ev -> projects.newProject())
      .separatorGroup("add");
  }

  @Order(2)
  @Bean
  @Qualifier("projectMenu")
  public Action projectLoadAction(ProjectLoadDialog dialog) {
    return new Action("Load project", Ionicons4IOS.OPEN, ev -> dialog.load())
      .separatorGroup("add");
  }

  @Order(3)
  @Bean
  @Qualifier("projectMenu")
  public Action closeAllProjectsAction(Projects projects) {
    return new Action("Close all projects", UniconsLine.BRUSH_ALT, ev -> projects.projects.clear())
      .separatorGroup("misc");
  }
}
