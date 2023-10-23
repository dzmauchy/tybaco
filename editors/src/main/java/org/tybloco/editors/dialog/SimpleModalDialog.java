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
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Window;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class SimpleModalDialog<R> extends ModalDialog<R> {

  public SimpleModalDialog(StringBinding title, StringBinding header, Window window, Node content, Supplier<R> result) {
    super(title, window, content, ButtonType.OK, ButtonType.CLOSE);
    headerTextProperty().bind(header);
    setResultConverter(result);
  }

  public SimpleModalDialog(StringBinding title, Window window, Node content, Supplier<R> result) {
    this(title, title, window, content, result);
  }

  public SimpleModalDialog(StringBinding title, StringBinding header, Node base, Node content, Supplier<R> result) {
    this(title, header, base.getScene().getWindow(), content, result);
  }

  public SimpleModalDialog(StringBinding title, Node base, Node content, Supplier<R> result) {
    this(title, title, base.getScene().getWindow(), content, result);
  }

  public SimpleModalDialog<R> configure(BiConsumer<SimpleModalDialog<R>, DialogPane> configurer) {
    configurer.accept(this, getDialogPane());
    return this;
  }
}
