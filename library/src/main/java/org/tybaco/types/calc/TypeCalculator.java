package org.tybaco.types.calc;

/*-
 * #%L
 * library
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

import com.google.common.primitives.Primitives;
import com.google.common.reflect.TypeResolver;
import com.google.common.reflect.TypeToken;
import org.tybaco.util.MiscOps;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Arrays.stream;
import static org.tybaco.types.calc.Types.*;
import static org.tybaco.util.ArrayOps.all;
import static org.tybaco.util.MiscOps.cast;

@SuppressWarnings("DuplicatedCode")
public final class TypeCalculator {

  private final HashMap<TypeVariable<?>, LinkedHashMap<Type, Boolean>> resolved;
  private final Method method;
  private final HashMap<String, Boolean> compatible;

  public TypeCalculator(Method method, Map<String, Type> args) {
    this.resolved = new HashMap<>(8, 0.5f);
    this.method = method;
    this.compatible = new HashMap<>(args.size(), 0.1f);
    for (var param : method.getParameters()) {
      var type = args.get(param.getName());
      if (type != null) {
        var formal = param.getParameterizedType();
        if (param.isVarArgs()) {
          var ct = param.getParameterizedType() instanceof GenericArrayType a
            ? a.getGenericComponentType()
            : param.getType().getComponentType();
          if (type instanceof VarArgs va) {
            var r = va.types().stream().allMatch(a -> isCompatible(ct, a, this::onMatch));
            compatible.put(param.getName(), r);
          } else {
            compatible.put(param.getName(), isCompatible(ct, type, this::onMatch));
          }
        } else {
          compatible.put(param.getName(), isCompatible(formal, type, this::onMatch));
        }
      }
    }
    args.forEach((k, v) -> compatible.putIfAbsent(k, FALSE));
  }

  private void onMatch(TypeVariable<?> v, Type t) {
    var map = resolved.computeIfAbsent(v, k -> new LinkedHashMap<>(2, 0.5f));
    map.put(t, TRUE);
  }

  public Stream<Method> methods() {
    return stream(method.getReturnType().getMethods()).filter(m -> !Modifier.isStatic(m.getModifiers()));
  }

  public Stream<Method> inputs() {
    return methods().filter(m -> m.getParameterCount() == 1 && m.getReturnType() == void.class);
  }

  public Stream<Method> outputs() {
    return methods().filter(m -> m.getParameterCount() == 0 && m.getReturnType() != void.class);
  }

  private TypeResolver prepareResolver() {
    var resolver = new TypeResolver();
    for (var e : resolved.entrySet()) {
      var var = e.getKey();
      var map = e.getValue();
      var t = switch (map.size()) {
        case 0 -> void.class;
        case 1 -> map.keySet().iterator().next();
        default -> u(map.keySet());
      };
      resolver = resolver.where(var, t);
    }
    return resolver;
  }

  public boolean isInputCompatible(String input, Type type) {
    return inputType(input).filter(t -> isCompatible(t, type)).isPresent();
  }

  public Optional<Type> inputType(String input) {
    return inputs()
      .filter(m -> m.getName().equals(input))
      .findFirst()
      .map(m -> TypeToken.of(method.getGenericReturnType()).method(m))
      .map(i -> {
        var resolver = prepareResolver();
        var pt = i.getParameters().get(0).getType();
        return resolver.resolveType(pt.getType());
      });
  }

  public Optional<Type> outputType(String output) {
    if ("*".equals(output)) {
      return Optional.of(outputType());
    }
    return outputs()
      .filter(m -> m.getName().equals(output))
      .findFirst()
      .map(m -> TypeToken.of(method.getGenericReturnType()).method(m))
      .map(i -> {
        var resolver = prepareResolver();
        var rt = i.getReturnType();
        var t = resolver.resolveType(rt.getType());
        return ground(t);
      });
  }

  public Type outputType() {
    var resolver = prepareResolver();
    var t = resolver.resolveType(method.getGenericReturnType());
    return ground(t);
  }

  public Stream<String> outputNames() {
    return Stream.concat(Stream.of("*"), outputs().map(Method::getName));
  }

  public boolean isCompatible(String arg) {
    return compatible.getOrDefault(arg, FALSE);
  }

  public void checkCompatibility(BiConsumer<String, Boolean> consumer) {
    compatible.forEach(consumer);
  }

  public static boolean isCompatible(Type formal, Type actual) {
    return isCompatible(formal, actual, (v, t) -> {});
  }

  public static boolean isCompatible(Type formal, Type actual, BiConsumer<TypeVariable<?>, Type> consumer) {
    if (formal instanceof WildcardType w) {
      var lb = w.getLowerBounds();
      if (lb.length != 0) {
        formal = wu(lb);
      }
    }
    return visit(formal, actual, null, TRUE, consumer);
  }

  private static boolean v(Type from, WildcardType t, TypeVars visited, Boolean covariant, BiConsumer<TypeVariable<?>, Type> consumer) {
    var result = false;
    for (var b : t.getUpperBounds()) {
      if (visit(from, b, visited, covariant, consumer)) {
        result = true;
      }
    }
    for (var b : t.getLowerBounds()) {
      visit(from, b, visited, covariant, consumer);
    }
    return result;
  }

  private static boolean v(Type from, UnionType t, TypeVars visited, Boolean covariant, BiConsumer<TypeVariable<?>, Type> consumer) {
    return t.types().stream().allMatch(b -> visit(from, b, visited, covariant, consumer));
  }

  private static boolean v(Class<?> f, Type to, Boolean covariant) {
    if (f.isPrimitive()) {
      if (to == Primitives.wrap(f)) {
        return true;
      } else if (f == int.class) {
        return to == char.class || to == short.class || to == byte.class;
      } else if (f == long.class) {
        return to == int.class || to == char.class || to == short.class || to == byte.class;
      } else if (f == double.class) {
        return to == float.class || to == long.class || to == int.class || to == char.class || to == short.class || to == byte.class;
      } else if (f == float.class) {
        return to == long.class || to == int.class || to == char.class || to == short.class || to == byte.class;
      } else if (f == short.class) {
        return to == byte.class;
      } else {
        return false;
      }
    } else if (Primitives.isWrapperType(f)) {
      return to instanceof Class<?> t && t.isPrimitive() && Primitives.wrap(t) == f;
    } else {
      if (to instanceof Class<?> t) {
        return covariant != null && (covariant ? f.isAssignableFrom(t) : t.isAssignableFrom(f));
      } else {
        var vars = f.getTypeParameters();
        if (vars.length == 0) {
          if (to instanceof GenericArrayType t) {
            if (f.isArray()) {
              return v(f.getComponentType(), t.getGenericComponentType(), covariant);
            } else {
              return v(f, Object[].class, covariant);
            }
          } else if (to instanceof ParameterizedType t) {
            var tc = (Class<?>) t.getRawType();
            return covariant != null && (covariant ? f.isAssignableFrom(tc) : tc.isAssignableFrom(f));
          } else {
            return false;
          }
        } else {
          return false;
        }
      }
    }
  }

  private static boolean v(GenericArrayType f, Type to, TypeVars visited, Boolean covariant, BiConsumer<TypeVariable<?>, Type> consumer) {
    if (to instanceof GenericArrayType t) {
      return visit(f.getGenericComponentType(), t.getGenericComponentType(), visited, covariant, consumer);
    } else if (to instanceof Class<?> c) {
      var ct = c.getComponentType();
      if (ct != null && !ct.isPrimitive()) {
        return visit(f.getGenericComponentType(), ct.getComponentType(), visited, covariant, consumer);
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  private static boolean v(ParameterizedType f, Type to, TypeVars visited, Boolean covariant, BiConsumer<TypeVariable<?>, Type> consumer) {
    var fa = f.getActualTypeArguments();
    if (to instanceof ParameterizedType t) {
      if (f.getRawType() != t.getRawType()) {
        if (covariant == null) {
          return false;
        }
        var fc = (Class<?>) f.getRawType();
        var tc = (Class<?>) t.getRawType();
        final TypeToken<?> token;
        if (covariant) {
          if (fc.isAssignableFrom(tc)) {
            token = TypeToken.of(to).getSupertype(cast(fc));
          } else {
            return false;
          }
        } else {
          if (tc.isAssignableFrom(fc)) {
            token = TypeToken.of(to).getSubtype(cast(fc));
          } else {
            return false;
          }
        }
        if (token.getType() instanceof ParameterizedType p) {
          return all(fa, p.getActualTypeArguments(), (e1, e2) -> visit(e1, e2, visited, null, consumer));
        } else {
          return false;
        }
      } else {
        return all(fa, t.getActualTypeArguments(), (e1, e2) -> visit(e1, e2, visited, null, consumer));
      }
    } else if (to instanceof Class<?> c) {
      var fc = (Class<?>) f.getRawType();
      if (fc == c || covariant == null) {
        return false;
      }
      if (covariant) {
        if (fc.isAssignableFrom(c)) {
          var token = TypeToken.of(c).getSupertype(cast(fc));
          if (token.getType() instanceof ParameterizedType t) {
            return all(fa, t.getActualTypeArguments(), (e1, e2) -> visit(e1, e2, visited, null, consumer));
          }
          return false;
        }
        return false;
      } else {
        return c.isAssignableFrom(fc);
      }
    } else {
      return false;
    }
  }

  private static boolean v(WildcardType f, Type to, TypeVars visited, BiConsumer<TypeVariable<?>, Type> consumer) {
    var lbs = flatten(f.getLowerBounds());
    if (lbs.length > 0) {
      return all(lbs, b -> visit(b, to, visited, FALSE, consumer));
    }
    return all(flatten(f.getUpperBounds()), b -> visit(b, to, visited, TRUE, consumer));
  }

  private static boolean v(TypeVariable<?> f, Type to, TypeVars visited, BiConsumer<TypeVariable<?>, Type> consumer) {
    if (visited != null && visited.contains(f)) {
      return true;
    }
    var newVisited = new TypeVars(f, visited);
    if (all(f.getBounds(), b -> visit(b, to, newVisited, TRUE, consumer))) {
      consumer.accept(f, to);
      return true;
    } else {
      return false;
    }
  }

  private static boolean visit(Type from, Type to, TypeVars visited, Boolean covariant, BiConsumer<TypeVariable<?>, Type> consumer) {
    if (from.equals(to)) {
      return true;
    }
    return switch (to) {
      case WildcardType t -> v(from, t, visited, covariant, consumer);
      case UnionType t -> v(from, t, visited, covariant, consumer);
      default -> switch (from) {
        case Class<?> f -> v(f, to, covariant);
        case GenericArrayType f -> v(f, to, visited, covariant, consumer);
        case ParameterizedType f -> v(f, to, visited, covariant, consumer);
        case WildcardType f -> v(f, to, visited, consumer);
        case TypeVariable<?> f -> v(f, to, visited, consumer);
        default -> false;
      };
    };
  }

  private static Stream<Type> flatten(Type type) {
    return type instanceof WildcardType w
      ? stream(w.getUpperBounds()).flatMap(TypeCalculator::flatten)
      : Stream.of(type);
  }

  private static Type[] flatten(Type[] types) {
    return stream(types)
      .flatMap(TypeCalculator::flatten)
      .distinct()
      .toArray(Type[]::new);
  }
}
