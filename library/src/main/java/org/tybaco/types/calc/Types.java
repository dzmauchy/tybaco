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

import java.lang.reflect.*;
import java.util.*;

import static org.tybaco.util.ArrayOps.all;

public final class Types {

  public static final Type[] EMPTY_TYPES = new Type[0];

  public static Type expand(Type type) {
    if (type instanceof Class<?> c) {
      var types = c.getTypeParameters();
      return types.length == 0 ? type : new ParameterizedTypeImpl(null, c, types);
    } else {
      return type;
    }
  }

  public static HashMap<TypeVariable<?>, Boolean> vars(Type type) {
    var set = new HashMap<TypeVariable<?>, Boolean>(2, 0.5f);
    vars(type, set);
    return set;
  }

  private static void vars(Type type, HashMap<TypeVariable<?>, Boolean> vars) {
    switch (type) {
      case TypeVariable<?> v -> {
        if (vars.putIfAbsent(v, Boolean.TRUE) == null) {
          for (var b : v.getBounds()) {
            vars(b, vars);
          }
        }
      }
      case GenericArrayType a -> vars(a.getGenericComponentType(), vars);
      case ParameterizedType p -> {
        vars(p.getOwnerType(), vars);
        for (var a : p.getActualTypeArguments()) {
          vars(a, vars);
        }
      }
      case WildcardType w -> {
        for (var b : w.getLowerBounds()) {
          vars(b, vars);
        }
        for (var b : w.getUpperBounds()) {
          vars(b, vars);
        }
      }
      default -> {}
    }
  }

  public static Type ground(Type type) {
    return ground(type, null);
  }

  private static Type ground(Type type, TypeVars vars) {
    return switch (type) {
      case GenericArrayType a -> {
        var ct = a.getGenericComponentType();
        var gct = ground(ct, vars);
        yield ct == gct ? type : new GenericArrayTypeImpl(gct);
      }
      case WildcardType w -> {
        var ub = w.getUpperBounds();
        var lb = w.getLowerBounds();
        var gub = ground(ub, vars);
        var glb = ground(lb, vars);
        yield gub == ub && glb == lb ? type : new WildcardTypeImpl(glb, gub);
      }
      case ParameterizedType p -> {
        var o = p.getOwnerType();
        var go = ground(o, vars);
        var args = p.getActualTypeArguments();
        var gArgs = ground(args, vars);
        yield o == go && args == gArgs ? type : new ParameterizedTypeImpl(go, p.getRawType(), gArgs);
      }
      case TypeVariable<?> v -> {
        if (vars != null && vars.contains(v)) {
          yield WildcardTypeImpl.ANY;
        } else {
          var nvars = new TypeVars(v, vars);
          var bounds = new LinkedHashSet<Type>();
          for (var b : ground(v.getBounds(), nvars)) {
            boundsForVar(ground(b, nvars), bounds);
          }
          var ub = bounds.toArray(Type[]::new);
          yield ub.length == 0 ? WildcardTypeImpl.ANY : new WildcardTypeImpl(EMPTY_TYPES, ub);
        }
      }
      default -> type;
    };
  }

  private static void boundsForVar(Type t, LinkedHashSet<Type> bounds) {
    if (t instanceof WildcardType w) {
      for (var b : w.getUpperBounds()) {
        if (b != Object.class) {
          boundsForVar(b, bounds);
        }
      }
    } else {
      bounds.add(t);
    }
  }

  private static Type[] ground(Type[] types, TypeVars vars) {
    var replaced = false;
    var newTypes = new Type[types.length];
    for (int i = 0; i < types.length; i++) {
      var t = types[i];
      var gt = ground(t, vars);
      if (t != gt) {
        replaced = true;
      }
      newTypes[i] = gt;
    }
    return replaced ? newTypes : types;
  }

  public static boolean isGround(Type type) {
    return switch (type) {
      case TypeVariable<?> ignored -> false;
      case ParameterizedType p -> isGround(p.getOwnerType()) && all(p.getActualTypeArguments(), Types::isGround);
      case GenericArrayType a -> isGround(a.getGenericComponentType());
      case WildcardType w -> all(w.getLowerBounds(), Types::isGround) && all(w.getUpperBounds(), Types::isGround);
      default -> true;
    };
  }

  public static GenericArrayType a(Type type) {
    return new GenericArrayTypeImpl(type);
  }

  public static ParameterizedType p(Class<?> raw, Type... params) {
    return new ParameterizedTypeImpl(null, raw, params);
  }

  public static ParameterizedType po(Type owner, Class<?> raw, Type... params) {
    return new ParameterizedTypeImpl(owner, raw, params);
  }

  public static WildcardType wu(Type... uppers) {
    return uppers.length == 0 ? WildcardTypeImpl.ANY : new WildcardTypeImpl(EMPTY_TYPES, uppers);
  }

  public static WildcardType wl(Type... lowers) {
    return lowers.length == 0 ? WildcardTypeImpl.ANY : new WildcardTypeImpl(lowers, EMPTY_TYPES);
  }

  public static UnionType u(Type... types) {
    return new UnionTypeImpl(Set.copyOf(Arrays.asList(types)));
  }

  public static UnionType u(Collection<? extends Type> types) {
    return new UnionTypeImpl(Set.copyOf(types));
  }

  public static VarArgs va(Type... types) {
    return new VarArgs(Set.copyOf(Arrays.asList(types)));
  }

  public static VarArgs va(Collection<? extends Type> types) {
    return new VarArgs(Set.copyOf(types));
  }

  @SuppressWarnings("unchecked")
  static <D extends GenericDeclaration> TypeVariable<D> v(D type, int index) {
    return (TypeVariable<D>) type.getTypeParameters()[index];
  }
}
