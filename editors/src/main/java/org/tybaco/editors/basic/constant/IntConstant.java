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
import javafx.scene.control.TextField;
import javafx.stage.Window;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tybaco.editors.control.GridPanes;
import org.tybaco.editors.dialog.ConstantEditDialog;
import org.tybaco.editors.model.Descriptor;
import org.tybaco.editors.model.LibConst;
import org.tybaco.editors.util.SeqMap;
import org.tybaco.editors.value.StringValue;
import org.tybaco.editors.value.Value;

import java.util.Optional;

@Qualifier("basic")
@Component
@Descriptor(id = "int", name = "Integer", icon = "mdi2n-numeric-0", description = "32 bit signed integer number")
public final class IntConstant implements LibConst {

  @Override
  public Optional<Value> edit(Window window, Value old) {
    return new Dlg(window, old).showAndWait();
  }

  @Override
  public Expression build(Value value) {
    return new IntegerLiteralExpr(extractValue(value));
  }

  private static String extractValue(Value value) {
    return switch (value) {
      case StringValue(var v) -> v;
      default -> throw new IllegalArgumentException(String.valueOf(value));
    };
  }

  private static class Dlg extends ConstantEditDialog {

    private final TextField field;

    public Dlg(Window window, Value old) {
      super(window);
      field = new TextField(old == null ? "0" : extractValue(old));
      getDialogPane().setContent(GridPanes.twoColumnPane(getClass(), new SeqMap<>("Number", field)));
    }

    @Override
    protected Value value() {
      return new StringValue(field.getText());
    }
  }
}
