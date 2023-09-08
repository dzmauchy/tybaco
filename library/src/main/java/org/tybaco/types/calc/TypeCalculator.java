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
import com.google.common.reflect.TypeToken;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.tybaco.types.calc.Types.add;
import static org.tybaco.types.calc.Types.cast;

@SuppressWarnings("DuplicatedCode")
public final class TypeCalculator {

  private final Method method;
  private final HashMap<TypeVariable<?>, LinkedHashMap<Type, Boolean>> resolved = new HashMap<>(8, 0.5f);
  private final HashMap<String, Boolean> compatible;

  public TypeCalculator(Method method, Map<String, Type> args) {
    this.method = method;
    this.compatible = new HashMap<>(args.size(), 0.1f);
    for (var param : method.getParameters()) {
      var type = args.get(param.getName());
      if (type != null) {
        var formal = param.getParameterizedType();
        var r = isCompatible(formal, type, (v, t) -> {
          var map = resolved.computeIfAbsent(v, k -> new LinkedHashMap<>(2, 0.5f));
          map.put(t, TRUE);
        });
        compatible.put(param.getName(), r);
      }
    }
  }

  public boolean isCompatible(String arg) {
    return compatible.getOrDefault(arg, FALSE);
  }

  public static boolean isCompatible(Type formal, Type actual) {
    return isCompatible(formal, actual, (v, t) -> {});
  }

  public static boolean isCompatible(Type formal, Type actual, BiConsumer<TypeVariable<?>, Type> consumer) {
    return visit(formal, actual, List.of(), TRUE, consumer);
  }

  private static boolean visit(Type from, Type to, List<TypeVariable<?>> visited, Boolean covariant, BiConsumer<TypeVariable<?>, Type> consumer) {
    if (to instanceof WildcardType t) {
      for (var b : t.getUpperBounds()) {
        if (visit(from, b, visited, covariant, consumer)) {
          return true;
        }
      }
      for (var b : t.getLowerBounds()) {
        visit(from, b, visited, covariant, consumer);
      }
      return false;
    } else if (from instanceof Class<?> f) {
      if (to == from) {
        return true;
      } else if (f.isPrimitive()) {
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
        if (to instanceof Class<?> t && covariant != null) {
          return covariant ? f.isAssignableFrom(t) : t.isAssignableFrom(f);
        } else {
          return false;
        }
      }
    } else if (from instanceof GenericArrayType f) {
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
    } else if (from instanceof ParameterizedType f) {
      var fa = f.getActualTypeArguments();
      if (to instanceof ParameterizedType t) {
        if (f.getRawType() != t.getRawType()) {
          if (covariant == null) {
            return false;
          }
          var fc = (Class<?>) f.getRawType();
          var tc = (Class<?>) t.getRawType();
          if (covariant) {
            if (!fc.isAssignableFrom(tc)) {
              return false;
            }
          } else {
            if (fc.isAssignableFrom(tc)) {
              return false;
            }
          }
        }
        var ta = t.getActualTypeArguments();
        if (fa.length != ta.length) {
          return false;
        }
        for (int i = 0; i < fa.length; i++) {
          if (!visit(fa[i], ta[i], visited, null, consumer)) {
            return false;
          }
        }
        return true;
      } else if (to instanceof Class<?> c) {
        var fc = (Class<?>) f.getRawType();
        if (fc == c || covariant == null || !covariant || !fc.isAssignableFrom(c)) {
          return false;
        }
        var token = TypeToken.of(c).getSupertype(cast(fc));
        if (token.getType() instanceof ParameterizedType t) {
          var ta = t.getActualTypeArguments();
          if (fa.length != ta.length) {
            return false;
          }
          for (int i = 0; i < fa.length; i++) {
            if (!visit(fa[i], ta[i], visited, null, consumer)) {
              return false;
            }
          }
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    } else if (from instanceof WildcardType f) {
      for (var b : flatten(f.getLowerBounds())) {
        if (!visit(b, to, visited, FALSE, consumer)) {
          return false;
        }
      }
      for (var b : flatten(f.getUpperBounds())) {
        if (!visit(b, to, visited, TRUE, consumer)) {
          return false;
        }
      }
      return true;
    } else if (from instanceof TypeVariable<?> f) {
      if (visited.contains(f)) {
        return true;
      }
      var newVisited = add(visited, f);
      for (var b : f.getBounds()) {
        if (!visit(b, to, newVisited, TRUE, consumer)) {
          return false;
        }
      }
      consumer.accept(f, to);
      return true;
    } else {
      return false;
    }
  }

  private static Stream<Type> flatten(Type type) {
    return type instanceof WildcardType w
      ? Arrays.stream(w.getUpperBounds()).flatMap(TypeCalculator::flatten)
      : Stream.of(type);
  }

  private static Type[] flatten(Type[] types) {
    return Arrays.stream(types)
      .flatMap(TypeCalculator::flatten)
      .distinct()
      .toArray(Type[]::new);
  }
}
