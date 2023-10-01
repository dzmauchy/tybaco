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

import javafx.scene.control.*;
import javafx.stage.Modality;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tybaco.editors.model.LibConst;
import org.tybaco.ui.model.Constant;
import org.tybaco.ui.model.Project;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static org.tybaco.editors.text.Texts.text;
import static org.tybaco.ui.main.MainStage.mainStage;

@Scope(SCOPE_PROTOTYPE)
@Component
public final class LibraryConstantsWindow extends Dialog<Constant> {

  public LibraryConstantsWindow(LibraryConstantsTree tree, Project project) {
    initModality(Modality.APPLICATION_MODAL);
    initOwner(mainStage());
    setResizable(true);
    getDialogPane().setContent(tree);
    getDialogPane().setPrefSize(900, 700);
    headerTextProperty().bind(text("Select a constant").map(v -> v + ":"));
    titleProperty().bind(text("Constants"));
    getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CLOSE);
    var applyButton = getDialogPane().lookupButton(ButtonType.APPLY);
    applyButton.setDisable(true);
    tree.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) ->
      applyButton.setDisable(nv == null || !(nv.getValue() instanceof LibConst))
    );
    setResultConverter(t -> t.getButtonData() == ButtonBar.ButtonData.APPLY ? constant(tree, project) : null);
  }

  private Constant constant(LibraryConstantsTree tree, Project project) {
    var item = tree.getSelectionModel().getSelectedItem();
    if (item != null && item.getValue() instanceof LibConst c) {
      var window = getDialogPane().getScene().getWindow();
      return c.edit(window, null)
        .map(v -> project.newConstant("name1", c.id(), v))
        .orElse(null);
    } else {
      return null;
    }
  }
}
