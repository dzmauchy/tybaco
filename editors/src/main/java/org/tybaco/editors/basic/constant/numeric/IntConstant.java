package org.tybaco.editors.basic.constant.numeric;

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
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tybaco.editors.control.GridPanes;
import org.tybaco.editors.dialog.ModalDialog;
import org.tybaco.editors.model.Descriptor;
import org.tybaco.editors.model.LibConst;
import org.tybaco.editors.util.SeqMap;

import java.util.Optional;

@Qualifier("basic")
@Component
@Descriptor(id = "int", name = "Integer", icon = "mdi2n-numeric-0", description = "32 bit signed integer number")
public final class IntConstant implements LibConst {

  @Override
  public Optional<IntegerLiteralExpr> edit(Node node, Expression oldValue) {
    var ov = oldValue instanceof IntegerLiteralExpr e ? e.getValue() : "0";
    final class D extends ModalDialog<String> {
      private D() {
        super(IntConstant.this.text("Integer"), node, ButtonType.OK, ButtonType.CLOSE);
        headerTextProperty().bind(text(name()));
        var field = new TextField(ov);
        getDialogPane().setContent(GridPanes.twoColumnPane(new SeqMap<>(text("Integer"), field)));
        setResultConverter(field::getText);
      }
    }
    return new D().showAndWait().map(IntegerLiteralExpr::new);
  }

  @Override
  public String type() {
    return "int";
  }

  @Override
  public Expression defaultValue() {
    return new IntegerLiteralExpr("0");
  }
}
