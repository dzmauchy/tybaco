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

import org.kordamp.ikonli.materialdesign2.MaterialDesignB;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.tybaco.ui.lib.action.Action;
import org.tybaco.ui.model.Project;

@Component
public class ProjectActions {

  @Bean
  @Qualifier("projectAction")
  @Order(1)
  public Action newBlockAction(Project project) {
    return new Action(null, MaterialDesignB.BABY_BOTTLE, "New block", ev -> {
      var factory = "com.example.factory";
      var method = "method";
      project.newBlock(project.guessBlockName(), factory, method, 0d, 0d);
    });
  }

  @Bean
  @Qualifier("projectAction")
  @Order(10)
  public Action diagramActionsSeparator() {
    return new Action();
  }

  @Bean
  @Qualifier("projectAction")
  @Order(11)
  public Action accordionVisibleAction(ProjectAccordion accordion) {
    return new Action(null, MaterialDesignB.BALLOON, "Accordion visibility")
      .selectionBoundTo(accordion.visibleProperty(), true);
  }
}
