package org.tybaco.editors.basic.block.collection;

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
import org.tybaco.editors.annotation.Input;
import org.tybaco.editors.annotation.Output;
import org.tybaco.editors.model.*;
import org.tybaco.editors.util.SeqMap;

import java.util.*;

@Qualifier("basic")
@Component
@Descriptor(
  id = "list",
  name = "List",
  icon = "eva-list",
  description = "Creates a generic immutable list"
)
@Input(
  id = "args",
  name = "Elements",
  icon = "mdi2l-list-status",
  description = "Elements of the list",
  vector = true,
  defaultValue = "null"
)
@Output(
  id = "self",
  name = "This list",
  icon = "mdal-alternate_email",
  description = "Resulting list of elements"
)
public final class ListBlock implements LibBlock {

  @Override
  public BlockResult build(String var, Map<String, List<Expression>> inputs) {
    return new BlockResult(
      new MethodCallExpr(
        new TypeExpr(new ClassOrInterfaceType(null, "java.util.List")),
        "of",
        NodeList.nodeList(inputs.getOrDefault("args", List.of()))
      ),
      new SeqMap<>("self", new NameExpr(var))
    );
  }
}
