package org.tybaco.editors.dialog;

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

import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Window;
import org.tybaco.editors.model.LibConst;
import org.tybaco.editors.value.Value;

import java.util.function.BiConsumer;

public final class ConstantEditDialog extends ModalDialog<Value> {

  private final ClassLoader classLoader;

  public ConstantEditDialog(LibConst libConst, Window window) {
    super(libConst.text(libConst.name()), window, ButtonType.OK, ButtonType.CLOSE);
    classLoader = libConst.textClassLoader();
    headerTextProperty().bind(text("Edit the constant").map(v -> v + ":"));
  }

  public ConstantEditDialog(LibConst libConst, Window window, Node content) {
    this(libConst, window);
    getDialogPane().setContent(content);
  }

  public ConstantEditDialog withDialogPane(BiConsumer<ConstantEditDialog, DialogPane> consumer) {
    consumer.accept(this, getDialogPane());
    return this;
  }

  @Override
  public ClassLoader textClassLoader() {
    return classLoader;
  }
}
