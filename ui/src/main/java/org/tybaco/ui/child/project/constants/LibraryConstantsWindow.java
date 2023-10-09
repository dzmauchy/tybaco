package org.tybaco.ui.child.project.constants;

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

import javafx.scene.control.ButtonType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tybaco.editors.dialog.ModalDialog;
import org.tybaco.editors.java.Expressions;
import org.tybaco.editors.model.LibConst;
import org.tybaco.editors.text.Texts;
import org.tybaco.ui.main.MainStage;
import org.tybaco.ui.model.Constant;
import org.tybaco.ui.model.Project;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Scope(SCOPE_PROTOTYPE)
@Component
public final class LibraryConstantsWindow extends ModalDialog<Constant> {

  public LibraryConstantsWindow(LibraryConstantsTree tree, Project project) {
    super(Texts.text("Constants"), MainStage.mainStage(), tree, ButtonType.OK, ButtonType.CLOSE);
    headerTextProperty().bind(text("Select a constant").map(v -> v + ":"));
    withDefaultButton(b -> b.disableProperty().bind(tree.nonLeafSelected));
    setResultConverter(() -> {
      var c = (LibConst) tree.getSelectionModel().getSelectedItem().getValue();
      return project.newConstant(c.id(), c.id(), Expressions.toText(c.defaultValue()));
    });
  }
}
