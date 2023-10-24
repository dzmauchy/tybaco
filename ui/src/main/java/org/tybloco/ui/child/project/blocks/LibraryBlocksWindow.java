package org.tybloco.ui.child.project.blocks;

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
import org.tybloco.editors.dialog.ModalDialog;
import org.tybloco.editors.model.LibBlock;
import org.tybloco.editors.text.Texts;
import org.tybloco.ui.main.MainStage;
import org.tybloco.ui.model.Block;
import org.tybloco.ui.model.Project;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Scope(SCOPE_PROTOTYPE)
@Component
public final class LibraryBlocksWindow extends ModalDialog<Block> {

  public LibraryBlocksWindow(LibraryBlocksTree tree, Project project) {
    super(Texts.text("Blocks"), MainStage.mainStage(), tree, ButtonType.OK, ButtonType.CLOSE);
    headerTextProperty().bind(text("Select a block").map(v -> v + ":"));
    withDefaultButton(b -> b.disableProperty().bind(tree.nonLeafSelected));
    setResultConverter(() -> {
      var c = (LibBlock) tree.getSelectedValue();
      return project.newBlock(c.id(), c.id(), 0d, 0d);
    });
  }
}
