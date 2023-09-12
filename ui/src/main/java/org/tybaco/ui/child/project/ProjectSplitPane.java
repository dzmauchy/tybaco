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

import javafx.collections.ListChangeListener.Change;
import javafx.scene.control.SplitPane;
import org.springframework.stereotype.Component;
import org.tybaco.ui.child.project.diagram.ProjectDiagram;

@Component
public class ProjectSplitPane extends SplitPane {

  private double lastDividerPosition = 0.7;

  public ProjectSplitPane(ProjectDiagram diagram, ProjectAccordion accordion) {
    super(diagram, accordion);
    setDividerPositions(lastDividerPosition);
    getDividers().addListener((Change<? extends Divider> c) -> {
      while (c.next()) {
        if (c.wasAdded()) {
          c.getAddedSubList().forEach(d -> {
            d.setPosition(lastDividerPosition);
            d.positionProperty().addListener((o, ov, nv) -> lastDividerPosition = nv.doubleValue());
          });
        }
      }
    });
    accordion.visibleProperty().addListener((o, ov, nv) -> {
      if (nv) {
        if (!getItems().contains(accordion)) {
          getItems().add(accordion);
        }
      } else {
        getItems().remove(accordion);
      }
    });
  }
}
