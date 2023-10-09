package org.tybaco.editors.basic.constant.network;

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

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tybaco.editors.model.Descriptor;
import org.tybaco.editors.model.SimpleLibConst;

import java.util.Optional;

@Qualifier("basic")
@Component
@Descriptor(id = "net_itf", name = "Network interface", icon = "fas-network-wired", description = "Network interface")
public final class NetworkInterface extends SimpleLibConst<MethodCallExpr> {

  @Override
  protected Optional<String> validate(Expression oldValue) {
    if (!(oldValue instanceof MethodCallExpr e)
      || !"getByName".equals(e.getNameAsString())
      || e.getArguments().size() != 1
      || !(e.getArguments().get(0) instanceof StringLiteralExpr le)) {
      return Optional.empty();
    } else {
      return Optional.of(le.getValue());
    }
  }

  @Override
  public String type() {
    return "java.net.NetworkInterface";
  }

  @Override
  protected MethodCallExpr expressionFromString(String v) {
    return new MethodCallExpr(
      new TypeExpr(new ClassOrInterfaceType(null, "java.net.NetworkInterface")),
      "getByName",
      NodeList.nodeList(new StringLiteralExpr(v))
    );
  }

  @Override
  protected String defaultStringValue() {
    return "loopback";
  }
}
