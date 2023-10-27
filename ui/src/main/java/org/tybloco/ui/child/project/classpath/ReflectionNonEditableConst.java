package org.tybloco.ui.child.project.classpath;

/*-
 * #%L
 * ui
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
import javafx.scene.Node;
import org.tybloco.editors.model.LibConst;

import java.lang.annotation.Annotation;
import java.util.Optional;

record ReflectionNonEditableConst(String id, String name, String icon, String description, String type, Expression expression) implements LibConst {

  ReflectionNonEditableConst(String id, Annotation annotation, String type, Expression expression) {
    this(id, value(annotation, "name"), value(annotation, "icon"), value(annotation, "description"), type, expression);
  }

  @Override
  public Optional<? extends Expression> edit(Node node, Expression oldValue) {
    return Optional.empty();
  }

  @Override
  public String type() {
    return null;
  }

  @Override
  public Expression defaultValue() {
    return expression;
  }

  private static String value(Annotation a, String method) {
    try {
      return a.annotationType().getMethod(method).invoke(a).toString();
    } catch (ReflectiveOperationException e) {
      throw new IllegalArgumentException(method, e);
    }
  }
}
