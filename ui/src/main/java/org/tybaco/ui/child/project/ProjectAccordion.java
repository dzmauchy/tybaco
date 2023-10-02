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

import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tybaco.editors.text.Texts;

import java.util.List;

@Component
public class ProjectAccordion extends Accordion {

  public ProjectAccordion(@Qualifier("forProjectAccordion") List<? extends Node> accordionNodes) {
    for (var node : accordionNodes) {
      var pane = new TitledPane(null, node);
      pane.textProperty().bind(Texts.text(node.getId()));
      getPanes().add(pane);
    }
    setExpandedPane(getPanes().getFirst());
  }
}
