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

final class Types {

  private static final Type[] EMPTY_TYPES = new Type[0];

  public static Type ground(Type type) {
    return ground(type, List.of());
  }

  private static Type ground(Type type, List<TypeVariable<?>> vars) {
    if (type instanceof Class<?>) {
      return type;
    } else if (type instanceof GenericArrayType a) {
      var ct = a.getGenericComponentType();
      var gct = ground(ct, vars);
      return ct == gct ? type : new A(gct);
    } else if (type instanceof WildcardType w) {
      var ub = w.getUpperBounds();
      var lb = w.getLowerBounds();
      var gub = ground(ub, vars);
      var glb = ground(lb, vars);
      return gub == ub && glb == lb ? type : new W(glb, gub);
    } else if (type instanceof ParameterizedType p) {
      var o = p.getOwnerType();
      var go = ground(o, vars);
      var args = p.getActualTypeArguments();
      var gArgs = ground(args, vars);
      return o == go && args == gArgs ? type : new P(go, p.getRawType(), gArgs);
    } else if (type instanceof TypeVariable<?> v) {
      if (vars.contains(v)) {
        return W.ANY;
      } else {
        var nvars = add(vars, v);
        var bounds = new LinkedHashSet<Type>();
        for (var b : ground(v.getBounds(), nvars)) {
          boundsForVar(ground(b, nvars), bounds);
        }
        var ub = bounds.toArray(Type[]::new);
        return ub.length == 0 ? W.ANY : new W(EMPTY_TYPES, ub);
      }
    } else {
      return type;
    }
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

  private static Type[] ground(Type[] types, List<TypeVariable<?>> vars) {
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
    if (type instanceof TypeVariable<?>) {
      return false;
    } else if (type instanceof Class<?>) {
      return true;
    } else if (type instanceof ParameterizedType pt) {
      if (!isGround(pt.getOwnerType())) {
        return false;
      }
      if (!isGround(pt.getRawType())) {
        return false;
      }
      for (var t : pt.getActualTypeArguments()) {
        if (!isGround(t)) {
          return false;
        }
      }
      return true;
    } else if (type instanceof GenericArrayType a) {
      return isGround(a.getGenericComponentType());
    } else if (type instanceof WildcardType w) {
      for (var t : w.getLowerBounds()) {
        if (!isGround(t)) {
          return false;
        }
      }
      for (var t : w.getUpperBounds()) {
        if (!isGround(t)) {
          return false;
        }
      }
      return true;
    } else {
      return true;
    }
  }

  @SuppressWarnings("unchecked")
  static <E> List<E> add(List<E> l, E e) {
    return switch (l.size()) {
      case 0 -> List.of(e);
      case 1 -> List.of(l.get(0), e);
      default -> {
        var array = new Object[l.size() + 1];
        var len = l.size();
        for (int i = 0; i < len; i++) {
          array[i] = l.get(i);
        }
        array[len] = e;
        yield (List<E>) Arrays.asList(array);
      }
    };
  }

  private record A(Type componentType) implements GenericArrayType {

    @Override
    public Type getGenericComponentType() {
      return componentType;
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof GenericArrayType a && componentType.equals(a.getGenericComponentType());
    }

    @Override
    public int hashCode() {
      return componentType.hashCode();
    }

    @Override
    public String getTypeName() {
      return componentType.getTypeName() + "[]";
    }

    @Override
    public String toString() {
      return getTypeName();
    }
  }

  private record P(Type owner, Type raw, Type[] args) implements ParameterizedType {

    @Override
    public Type[] getActualTypeArguments() {
      return args;
    }

    @Override
    public Type getRawType() {
      return raw;
    }

    @Override
    public Type getOwnerType() {
      return owner;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(owner) ^ Arrays.asList(args).hashCode() ^ raw.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof ParameterizedType p
        && Objects.equals(owner, p.getOwnerType())
        && raw.equals(p.getRawType())
        && Arrays.equals(args, p.getActualTypeArguments());
    }

    @Override
    public String getTypeName() {
      var builder = new StringBuilder(64);
      if (owner != null) {
        builder.append(owner.getTypeName()).append('.');
      }
      builder.append(raw.getTypeName()).append("<");
      if (args.length > 0) {
        builder.append(args[0].getTypeName());
        for (int i = 1; i < args.length; i++) {
          builder.append(',').append(args[i].getTypeName());
        }
      }
      builder.append('>');
      return builder.toString();
    }

    @Override
    public String toString() {
      return getTypeName();
    }
  }

  private record W(Type[] lb, Type[] ub) implements WildcardType {

    public static final W ANY = new W(EMPTY_TYPES, new Type[] {Object.class});

    @Override
    public Type[] getUpperBounds() {
      return ub;
    }

    @Override
    public Type[] getLowerBounds() {
      return lb;
    }

    @Override
    public int hashCode() {
      return Arrays.asList(lb).hashCode() ^ Arrays.asList(ub).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof WildcardType w && Arrays.equals(ub, w.getUpperBounds()) && Arrays.equals(lb, w.getLowerBounds());
    }

    @Override
    public String getTypeName() {
      if (lb.length == 0 && (ub.length == 0 || ub[0] == Object.class)) {
        return "?";
      } else {
        var builder = new StringBuilder(32);
        builder.append('?');
        if (ub.length != 0) {
          builder.append(" extends ").append(ub[0].getTypeName());
          for (int i = 1; i < ub.length; i++) {
            builder.append(" & ").append(ub[i].getTypeName());
          }
        }
        if (lb.length != 0) {
          builder.append(" super ").append(lb[0].getTypeName());
          for (int i = 1; i < lb.length; i++) {
            builder.append(" & ").append(lb[i].getTypeName());
          }
        }
        return builder.toString();
      }
    }

    @Override
    public String toString() {
      return getTypeName();
    }
  }
}
