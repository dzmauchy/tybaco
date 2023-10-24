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

import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.materialdesign2.MaterialDesignB;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.tybloco.editors.action.Action;
import org.tybloco.ui.child.project.blocks.LibraryBlocksWindow;
import org.tybloco.ui.child.project.classpath.Editors;
import org.tybloco.ui.child.project.constants.LibraryConstantsWindow;
import org.tybloco.ui.model.Project;
import org.tybloco.xml.Xml;

@Component
public class ProjectActions {

  @Bean
  @Qualifier("projectAction")
  @Order(1)
  public Action newBlockAction(ObjectProvider<LibraryBlocksWindow> win, Editors editors) {
    return new Action(null, MaterialDesignB.BABY_BOTTLE, "New block", ev -> {
      var window = win.getObject();
      window.show();
    }).separatorGroup("block").disabled(editors.blockLibs.isNull());
  }

  @Bean
  @Qualifier("projectAction")
  @Order(2)
  public Action newConstantAction(ObjectProvider<LibraryConstantsWindow> win, Editors editors) {
    return new Action(null, MaterialDesignB.BULLSEYE, "New constant", ev -> {
      var window = win.getObject();
      window.show();
    }).separatorGroup("constant").disabled(editors.constLibs.isNull());
  }

  @Bean
  @Qualifier("projectAction")
  @Order(1001)
  public Action saveProjectAction(ProjectSaveDialog dialog, Project project) {
    return new Action(null, BootstrapIcons.SAVE, "Save project", ev ->
      dialog.showAndWait().ifPresent(f -> Xml.saveTo(f, "project", project::saveTo))
    ).separatorGroup("file");
  }

  @Bean
  @Qualifier("projectAction")
  @Order(10001)
  public Action accordionVisibleAction(ProjectAccordion accordion) {
    return new Action(null, MaterialDesignB.BOOK_OPEN, "Accordion visibility")
      .selectionBoundTo(accordion.visibleProperty(), true)
      .separatorGroup("visibility");
  }
}
