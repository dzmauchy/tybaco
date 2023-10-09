package org.tybaco.editors.basic.constant.primitive;

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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tybaco.editors.model.Descriptor;
import org.tybaco.editors.model.SimpleLibConst;

import java.util.Optional;

@Qualifier("basic")
@Component
@Descriptor(id = "int", name = "Integer", icon = "mdi2n-numeric", description = "32 bit signed integer number")
public final class IntConstant extends SimpleLibConst<IntegerLiteralExpr> {

  @Override
  public String type() {
    return "int";
  }

  @Override
  protected IntegerLiteralExpr expressionFromString(String v) {
    return new IntegerLiteralExpr(v);
  }

  @Override
  protected String defaultStringValue() {
    return "0";
  }

  @Override
  protected Optional<String> validate(Expression expression) {
    return expression instanceof IntegerLiteralExpr e ? Optional.of(e.getValue()) : Optional.empty();
  }
}
