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
import javafx.scene.Node;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tybaco.editors.model.Descriptor;
import org.tybaco.editors.model.LibConst;

import java.util.Optional;

@Qualifier("basic")
@Component
@Descriptor(id = "net_itf", name = "Network interface", icon = "fas-network-wired", description = "Network interface")
public final class NetworkInterface implements LibConst {

  @Override
  public Optional<? extends Expression> edit(Node node, Expression oldValue) {
    return Optional.empty();
  }

  @Override
  public String type() {
    return "java.net.NetworkInterface";
  }

  @Override
  public Expression defaultValue() {
    return new MethodCallExpr(
      new TypeExpr(new ClassOrInterfaceType(null, "java.net.NetworkInterface")),
      "getByName",
      NodeList.nodeList(new StringLiteralExpr("loopback"))
    );
  }
}
