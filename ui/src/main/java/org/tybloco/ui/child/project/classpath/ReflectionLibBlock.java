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
import com.github.javaparser.serialization.JavaParserJsonDeserializer;
import org.tybloco.editors.model.*;
import org.tybloco.editors.util.SeqMap;

import javax.json.Json;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class ReflectionLibBlock implements LibBlock {

  private final String declaringClass;
  private final String factory;
  private final String id;
  private final String name;
  private final String icon;
  private final String description;
  private final LinkedHashMap<String, LibInput> inputs;
  private final LinkedHashMap<String, LibOutput> outputs;
  private final LinkedHashMap<String, Function<String, Expression>> outputExpressions;

  public ReflectionLibBlock(String blockId, Executable executable, Annotation annotation) {
    declaringClass = executable.getDeclaringClass().getName();
    factory = executable instanceof Method m ? m.getName() : "new";
    id = blockId;
    inputs = LinkedHashMap.newLinkedHashMap(executable.getParameterCount());
    try {
      name = annotation.annotationType().getMethod("name").invoke(annotation).toString();
      icon = annotation.annotationType().getMethod("icon").invoke(annotation).toString();
      description = annotation.annotationType().getMethod("description").invoke(annotation).toString();
      switch (executable) {
        case Method m -> {
          outputs = LinkedHashMap.newLinkedHashMap(1);
          outputExpressions = LinkedHashMap.newLinkedHashMap(1);
          outputs.put("self", new LibOutput("self", "This value", "之", "This value", m.getReturnType().getName()));
          outputExpressions.put("self", NameExpr::new);
        }
        case Constructor<?> c -> {
          var outputMethods = new LinkedHashMap<Method, Annotation>();
          var outputFields = new LinkedHashMap<Field, Annotation>();
          for (var m : c.getDeclaringClass().getMethods()) out(m).ifPresent(a -> outputMethods.put(m, a));
          for (var f : c.getDeclaringClass().getFields()) out(f).ifPresent(a -> outputFields.put(f, a));
          outputs = LinkedHashMap.newLinkedHashMap(1 + outputMethods.size() + outputFields.size());
          outputExpressions = LinkedHashMap.newLinkedHashMap(1 + outputMethods.size() + outputFields.size());
          outputs.put("self", new LibOutput("self", "This value", "之", "This value", c.getDeclaringClass().getName()));
          outputExpressions.put("self", NameExpr::new);
          for (var e : outputMethods.entrySet()) {
            outputs.put(e.getKey().getName(), output(e.getKey().getName(), e.getValue(), e.getKey().getReturnType()));
            outputExpressions.put(e.getKey().getName(), v -> new MethodCallExpr(new NameExpr(v), e.getKey().getName()));
          }
          for (var e : outputFields.entrySet()) {
            outputs.put(e.getKey().getName(), output(e.getKey().getName(), e.getValue(), e.getKey().getType()));
            outputExpressions.put(e.getKey().getName(), v -> new FieldAccessExpr(new NameExpr(v), e.getKey().getName()));
          }
        }
      }
      for (var p : executable.getParameters()) {
        inputs.put(p.getName(), input(p));
      }
    } catch (ReflectiveOperationException e) {
      throw new IllegalArgumentException(annotation.toString(), e);
    }
  }

  @Override
  public void forEachInput(BiConsumer<String, LibInput> consumer) {
    inputs.forEach(consumer);
  }

  @Override
  public void forEachOutput(BiConsumer<String, LibOutput> consumer) {
    outputs.forEach(consumer);
  }

  @Override
  public BlockResult build(String var, Map<String, List<Expression>> inputs, boolean safeCast) {
    var args = new NodeList<Expression>();
    this.inputs.forEach((k, v) -> {
      var es = inputs.get(k);
      if (es != null && !es.isEmpty()) {
        if (v.vector()) {
          for (var expr : es) {
            if (safeCast) {
              args.add(new CastExpr(new ClassOrInterfaceType(null, v.type()), expr));
            } else {
              args.add(expr);
            }
          }
        } else {
          if (safeCast) {
            args.add(new CastExpr(new ClassOrInterfaceType(null, v.type()), es.getFirst()));
          } else {
            args.add(es.getFirst());
          }
        }
      } else if ("".equals(v.defaultValue())) {
        var expr = switch (v.type()) {
          case "int" -> new IntegerLiteralExpr("0");
          case "short" -> new CastExpr(PrimitiveType.shortType(), new IntegerLiteralExpr("0"));
          case "byte" -> new CastExpr(PrimitiveType.byteType(), new IntegerLiteralExpr("0"));
          case "long" -> new LongLiteralExpr("0");
          case "float" -> new DoubleLiteralExpr("0f");
          case "double" -> new DoubleLiteralExpr("0d");
          case "boolean" -> new BooleanLiteralExpr(false);
          case "char" -> new CharLiteralExpr("0");
          default -> new NullLiteralExpr();
        };
        args.add(expr);
      } else {
        if (v.defaultValue().startsWith("{\"!")) {
          var d = new JavaParserJsonDeserializer();
          var expr = d.deserializeObject(Json.createReader(new StringReader(v.defaultValue())));
          args.add((Expression) expr);
        } else if (v.defaultValue().startsWith("$")) {
          args.add(new NameExpr(v.defaultValue()));
        } else {
          throw new IllegalArgumentException(v.defaultValue());
        }
      }
    });
    var declaringType = new ClassOrInterfaceType(null, declaringClass);
    var expr = "new".equals(factory) ? new ObjectCreationExpr(null, declaringType, args) : new MethodCallExpr(new TypeExpr(declaringType), factory, args);
    var outputs = LinkedHashMap.<String, Expression>newLinkedHashMap(this.outputs.size());
    this.outputExpressions.forEach((k, v) -> outputs.put(k, v.apply(var)));
    return new BlockResult(expr, new SeqMap<>(outputs));
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String icon() {
    return icon;
  }

  @Override
  public String description() {
    return description;
  }

  private static LibInput input(Parameter parameter) throws ReflectiveOperationException {
    Annotation ia = null, iia = null;
    for (var a : parameter.getAnnotations()) {
      switch (a.annotationType().getName()) {
        case "org.tybloco.runtime.meta.Input" -> ia = a;
        case "org.tybloco.runtime.meta.InternalInput" -> iia = a;
      }
    }
    var vararg = parameter.isVarArgs();
    var type = vararg ? parameter.getType().getComponentType().getName() : parameter.getType().getName();
    if (ia == null) {
      var name = parameter.getName();
      var icon = "酋";
      var description = parameter.getName();
      var defaultValue = iia == null ? "" : iia.annotationType().getMethod("value").invoke(iia).toString();
      return new LibInput(parameter.getName(), name, icon, description, vararg, defaultValue, type);
    } else {
      var name = ia.annotationType().getMethod("name").invoke(ia).toString();
      var icon = ia.annotationType().getMethod("icon").invoke(ia).toString();
      var description = ia.annotationType().getMethod("description").invoke(ia).toString();
      var v = ia.annotationType().getMethod("value").invoke(ia).toString();
      var defaultValue = "".equals(v) && iia != null ? iia.annotationType().getMethod("value").invoke(iia).toString() : v;
      return new LibInput(parameter.getName(), name, icon, description, vararg, defaultValue, type);
    }
  }

  private static LibOutput output(String id, Annotation ann, Class<?> type) throws ReflectiveOperationException {
    return new LibOutput(
      id,
      ann.annotationType().getMethod("name").invoke(ann).toString(),
      ann.annotationType().getMethod("icon").invoke(ann).toString(),
      ann.annotationType().getMethod("description").invoke(ann).toString(),
      type.getName()
    );
  }

  private static Optional<Annotation> out(Method method) {
    if (method.getParameterCount() != 0) return Optional.empty();
    if (Modifier.isStatic(method.getModifiers())) return Optional.empty();
    if (method.getReturnType() == void.class) return Optional.empty();
    for (var a : method.getAnnotations()) {
      if (a.annotationType().getName().equals("org.tybloco.runtime.meta.Output")) {
        return Optional.of(a);
      }
    }
    return Optional.empty();
  }

  private static Optional<Annotation> out(Field field) {
    if (Modifier.isStatic(field.getModifiers())) return Optional.empty();
    for (var a : field.getAnnotations()) {
      if (a.annotationType().getName().equals("org.tybloco.runtime.meta.Output")) {
        return Optional.of(a);
      }
    }
    return Optional.empty();
  }
}
