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
import java.util.stream.Collectors;

import static org.tybaco.util.ArrayOps.all;

public final class Types {

  private static final Type[] EMPTY_TYPES = new Type[0];

  public static Type expand(Type type) {
    if (type instanceof Class<?> c) {
      var types = c.getTypeParameters();
      return types.length == 0 ? type : new P(null, c, types);
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
        yield ct == gct ? type : new A(gct);
      }
      case WildcardType w -> {
        var ub = w.getUpperBounds();
        var lb = w.getLowerBounds();
        var gub = ground(ub, vars);
        var glb = ground(lb, vars);
        yield gub == ub && glb == lb ? type : new W(glb, gub);
      }
      case ParameterizedType p -> {
        var o = p.getOwnerType();
        var go = ground(o, vars);
        var args = p.getActualTypeArguments();
        var gArgs = ground(args, vars);
        yield o == go && args == gArgs ? type : new P(go, p.getRawType(), gArgs);
      }
      case TypeVariable<?> v -> {
        if (vars != null && vars.contains(v)) {
          yield W.ANY;
        } else {
          var nvars = new TypeVars(v, vars);
          var bounds = new LinkedHashSet<Type>();
          for (var b : ground(v.getBounds(), nvars)) {
            boundsForVar(ground(b, nvars), bounds);
          }
          var ub = bounds.toArray(Type[]::new);
          yield ub.length == 0 ? W.ANY : new W(EMPTY_TYPES, ub);
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

    public static final W ANY = new W(EMPTY_TYPES, new Type[]{Object.class});

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

  private record U(Set<? extends Type> types) implements UnionType {

    @Override
    public int hashCode() {
      return types.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof UnionType u && types.equals(u.types());
    }

    @Override
    public Type[] getTypes() {
      return types.toArray(Type[]::new);
    }

    @Override
    public String getTypeName() {
      return types.stream().map(Type::getTypeName).collect(Collectors.joining(" | "));
    }

    @Override
    public String toString() {
      return getTypeName();
    }
  }

  public record VarArgs(Set<? extends Type> types) implements Type {
  }

  @SuppressWarnings("unchecked")
  static <T> T cast(Object v) {
    return (T) v;
  }

  public static GenericArrayType a(Type type) {
    return new A(type);
  }

  public static ParameterizedType p(Class<?> raw, Type... params) {
    return new P(null, raw, params);
  }

  public static ParameterizedType po(Type owner, Class<?> raw, Type... params) {
    return new P(owner, raw, params);
  }

  public static WildcardType wu(Type... uppers) {
    return uppers.length == 0 ? W.ANY : new W(EMPTY_TYPES, uppers);
  }

  public static WildcardType wl(Type... lowers) {
    return lowers.length == 0 ? W.ANY : new W(lowers, EMPTY_TYPES);
  }

  public static UnionType u(Type... types) {
    return new U(Set.copyOf(Arrays.asList(types)));
  }

  public static UnionType u(Collection<? extends Type> types) {
    return new U(Set.copyOf(types));
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
