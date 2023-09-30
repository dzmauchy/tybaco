package org.tybaco.editors.basic.constant;

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

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.stage.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tybaco.editors.model.Descriptor;
import org.tybaco.editors.model.LibConst;
import org.tybaco.editors.value.StringValue;
import org.tybaco.editors.value.Value;

import static javafx.scene.control.ButtonBar.ButtonData.APPLY;

@Qualifier("basic")
@Component
@Descriptor(id = "int", name = "Integer", icon = "mdi2n-numeric-0", description = "32 bit signed integer number")
public final class IntConstant implements LibConst {

  @Override
  public Value edit(Window window, Value old) {
    var dialog = new Dialog<Value>();
    dialog.initOwner(window);
    dialog.initModality(Modality.WINDOW_MODAL);
    dialog.initStyle(StageStyle.DECORATED);
    var text = new TextField(old == null ? "0" : extractValue(old));
    dialog.setResultConverter(t -> t.getButtonData() == APPLY ? new StringValue(text.getText()) : null);
    return null;
  }

  @Override
  public Expression build(Value value) {
    return new IntegerLiteralExpr(extractValue(value));
  }

  private String extractValue(Value value) {
    return switch (value) {
      case null -> throw new NullPointerException("value");
      case StringValue v -> v.value();
      default -> throw new IllegalArgumentException(value.getClass().getName());
    };
  }
}
