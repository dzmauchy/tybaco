package org.tybloco.editors.dialog;

/*-
 * #%L
 * editors
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

import javafx.beans.binding.StringBinding;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Window;
import org.tybloco.editors.text.TextSupport;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ModalDialog<R> extends Dialog<R> implements TextSupport {

  public ModalDialog(StringBinding title, Window owner, ButtonType... buttonTypes) {
    initOwner(owner);
    initModality(Modality.WINDOW_MODAL);
    setResizable(true);
    getDialogPane().setPrefSize(900, 700);
    getDialogPane().getButtonTypes().addAll(buttonTypes);
    titleProperty().bind(title);
  }

  public ModalDialog(StringBinding title, Node owner, ButtonType... buttonTypes) {
    initOwner(owner.getScene().getWindow());
    initModality(Modality.WINDOW_MODAL);
    setResizable(true);
    getDialogPane().setPrefSize(900, 700);
    getDialogPane().getButtonTypes().addAll(buttonTypes);
    titleProperty().bind(title);
  }

  public ModalDialog(StringBinding title, Window owner, Node content, ButtonType... buttonTypes) {
    this(title, owner, buttonTypes);
    getDialogPane().setContent(content);
  }

  public ModalDialog(StringBinding title, Node owner, Node content, ButtonType... buttonTypes) {
    this(title, owner, buttonTypes);
    getDialogPane().setContent(content);
  }

  public void withButton(ButtonType type, Consumer<Button> consumer) {
    if (getDialogPane().lookupButton(type) instanceof Button b) {
      consumer.accept(b);
    }
  }

  public void withDefaultButton(Consumer<Button> consumer) {
    getDialogPane().getButtonTypes().forEach(t -> {
      if (getDialogPane().lookupButton(t) instanceof Button b && t.getButtonData().isDefaultButton()) {
        consumer.accept(b);
      }
    });
  }

  protected void setResultConverter(Supplier<R> result) {
    setResultConverter(t -> t.getButtonData().isDefaultButton() ? result.get() : null);
  }

  public Optional<R> showAndWait(Supplier<R> result) {
    setResultConverter(result);
    return showAndWait();
  }
}
