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

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import org.tybloco.editors.dialog.SimpleModalDialog;
import org.tybloco.editors.model.LibConst;
import org.tybloco.editors.util.SeqMap;
import org.tybloco.ui.util.Validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static org.tybloco.editors.control.GridPanes.twoColumnPane;

public interface ConstantEditors {

  static LibConst libConst(String id, Method method, Annotation a) {
    return switch (method.getParameterTypes()[0].getName()) {
      case "int" -> new LC(method, a, id, "0",
        IntegerLiteralExpr::new,
        e -> e instanceof IntegerLiteralExpr expr ? expr.getValue() : "0",
        validate(v -> () -> new IntegerLiteralExpr(v).asNumber() instanceof Integer ? null : "not an integer")
      );
      case "long" -> new LC(method, a, id, "0",
        LongLiteralExpr::new,
        e -> e instanceof LongLiteralExpr expr ? expr.getValue() : "0L",
        validate(v -> () -> new LongLiteralExpr(v).asNumber() instanceof Long ? null : "not a long")
      );
      case "byte" -> new LC(method, a, id, "0",
        v -> new CastExpr(PrimitiveType.byteType(), new IntegerLiteralExpr(v)),
        e -> (e instanceof CastExpr ce) && (ce.getExpression() instanceof IntegerLiteralExpr le) ? le.getValue() : "0",
        validate(v -> () -> between(new IntegerLiteralExpr(v).asNumber(), Byte.MIN_VALUE, Byte.MAX_VALUE) ? null : "not a byte")
      );
      case "short" -> new LC(method, a, id, "0",
        v -> new CastExpr(PrimitiveType.shortType(), new IntegerLiteralExpr(v)),
        e -> (e instanceof CastExpr ce) && (ce.getExpression() instanceof IntegerLiteralExpr le) ? le.getValue() : "0",
        validate(v -> () -> between(new IntegerLiteralExpr(v).asNumber(), Short.MIN_VALUE, Short.MAX_VALUE) ? null : "not a short number")
      );
      case "char" -> new LC(method, a, id, "0",
        CharLiteralExpr::new,
        e -> e instanceof CharLiteralExpr le ? le.getValue() : "\\u0000",
        validate(v -> () -> {
          new CharLiteralExpr(v).asChar();
          return null;
        })
      );
      case "float" -> new LC(method, a, id, "0",
        DoubleLiteralExpr::new,
        e -> e instanceof DoubleLiteralExpr le ? le.getValue() : "0",
        validate(v -> () -> between(new DoubleLiteralExpr(v).asDouble(), Float.MIN_VALUE, Float.MAX_VALUE) ? null : "not a float")
      );
      case "double" -> new LC(method, a, id, "0",
        DoubleLiteralExpr::new,
        e -> e instanceof DoubleLiteralExpr le ? le.getValue() : "0",
        validate(v -> () -> {
          new DoubleLiteralExpr(v).asDouble();
          return null;
        })
      );
      case "boolean" -> new LC(method, a, id, "false",
        v -> new BooleanLiteralExpr("true".equals(v) || "1".equals(v)),
        e -> e instanceof BooleanLiteralExpr le ? Boolean.toString(le.getValue()) : "false",
        validate(v -> () -> "true".equals(v) || "false".equals(v) || "1".equals(v) || "0".equals(v) ? null : "not a boolean")
      );
      case "java.lang.String" -> new LC(method, a, id, "",
        StringLiteralExpr::new,
        e -> e instanceof StringLiteralExpr le ? le.getValue() : "",
        Validation.OK::new
      );
      default -> null;
    };
  }

  record LC(
    String className,
    String methodName,
    String type,
    String id,
    String name,
    String icon,
    String description,
    String defaultVal,
    Function<String, Expression> ff,
    Function<Expression, String> rf,
    Function<String, Validation<String>> validator
  ) implements LibConst {

    LC(Method m, Annotation a, String id, String v, Function<String, Expression> ff, Function<Expression, String> rf, Function<String, Validation<String>> validator) {
      this(
        m.getDeclaringClass().getName(),
        m.getName(),
        m.getReturnType().getName(),
        id,
        ReflectionConst.value(a, "name"),
        ReflectionConst.value(a, "icon"),
        ReflectionConst.value(a, "description"),
        v,
        ff,
        rf,
        validator
      );
    }

    @Override
    public Optional<? extends Expression> edit(Node node, Expression oldValue) {
      var textField = new TextField(rf.apply(unwrapExpression(oldValue)));
      var content = twoColumnPane(new SeqMap<>(text("Value"), textField));
      return new SimpleModalDialog<>(
        text("Constant"),
        node, content,
        () -> wrapExpression(ff.apply(textField.getText()))
      ).showAndWait();
    }

    @Override
    public String type() {
      return type;
    }

    @Override
    public Expression defaultValue() {
      return wrapExpression(ff.apply(defaultVal));
    }

    @Override
    public String shortText(Expression expression) {
      return unwrapExpression(expression).toString();
    }

    private Expression unwrapExpression(Expression expression) {
      if (expression instanceof MethodCallExpr mce
        && mce.getScope().isPresent()
        && mce.getScope().get() instanceof TypeExpr te
        && te.getType() instanceof ClassOrInterfaceType cit
        && cit.getName().asString().equals(className)
        && mce.getName().asString().equals(methodName)
        && mce.getArguments().size() == 1
      ) {
        return mce.getArguments().get(0);
      } else {
        return ff.apply(defaultVal);
      }
    }

    private MethodCallExpr wrapExpression(Expression expression) {
      return new MethodCallExpr(
        new TypeExpr(new ClassOrInterfaceType(null, className)),
        methodName,
        new NodeList<>(expression)
      );
    }
  }

  private static Function<String, Validation<String>> validate(Function<String, Callable<String>> function) {
    return v -> {
      try {
        var r = function.apply(v).call();
        return r == null ? new Validation.OK<>(v) : new Validation.Failure<>(r);
      } catch (Exception e) {
        return new Validation.Failure<>(e.getMessage());
      }
    };
  }

  private static boolean between(Number number, int low, int high) {
    return number instanceof Integer i && i >= low && i <= high;
  }

  private static boolean between(double v, double min, double max) {
    return v >= min && v <= max;
  }
}
