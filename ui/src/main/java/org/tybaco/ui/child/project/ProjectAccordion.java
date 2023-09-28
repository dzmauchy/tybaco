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

import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import org.springframework.stereotype.Component;
import org.tybaco.ui.child.project.constants.ProjectConstantsPane;
import org.tybaco.ui.child.project.deps.ProjectDepsPane;
import org.tybaco.editors.text.Texts;

@Component
public class ProjectAccordion extends Accordion {

  private final TitledPane constantsPane;
  private final TitledPane librariesPane;

  public ProjectAccordion(ProjectConstantsPane constants, ProjectDepsPane libraries) {
    constantsPane = new TitledPane(null, constants);
    constantsPane.textProperty().bind(Texts.text("Constants"));
    librariesPane = new TitledPane(null, libraries);
    librariesPane.textProperty().bind(Texts.text("Dependencies"));
    getPanes().addAll(constantsPane, librariesPane);
    setExpandedPane(constantsPane);
  }
}
