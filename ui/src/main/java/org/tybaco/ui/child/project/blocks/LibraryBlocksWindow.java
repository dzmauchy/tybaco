package org.tybaco.ui.child.project.blocks;

import javafx.scene.control.ButtonType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tybaco.editors.dialog.ModalDialog;
import org.tybaco.editors.model.LibBlock;
import org.tybaco.editors.text.Texts;
import org.tybaco.ui.main.MainStage;
import org.tybaco.ui.model.Block;
import org.tybaco.ui.model.Project;

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
