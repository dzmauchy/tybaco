package org.tybaco.types.resolver;

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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.IntersectionTypeBinding18;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.VoidTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.WildcardBinding;
import org.tybaco.types.resolver.Method.Arg;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static org.eclipse.jdt.internal.compiler.lookup.Scope.convertEliminatingTypeVariables;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class TypeResolverResults {

    private final CompilationUnitScope scope;
    final TreeMap<String, TypeBinding> types = new TreeMap<>();
    final TreeMap<String, List<String>> errors = new TreeMap<>();
    final TreeMap<String, List<String>> warns = new TreeMap<>();
    final TreeMap<String, List<String>> infos = new TreeMap<>();

    public ResolvedType getType(String name) {
        var type = types.get(name);
        return new ResolvedType(type == null ? TypeBinding.VOID : type);
    }

    public ResolvedType typeOf(String... parts) {
        var compoundName = new char[parts.length][];
        for (int i = 0; i < parts.length; i++) {
            compoundName[i] = parts[i].toCharArray();
        }
        return new ResolvedType(scope.getType(compoundName, parts.length));
    }

    public ResolvedType array(ResolvedType type, int dimension) {
        return new ResolvedType(scope.createArrayType(type.type, dimension));
    }

    public ResolvedType boxed(ResolvedType type) {
        return new ResolvedType(scope.boxing(type.type));
    }

    private static Method method(ResolvedType type, MethodBinding m) {
        var args = IntStream.range(0, m.parameters.length)
                .mapToObj(i -> new Arg(m.parameters[i], i))
                .toList();
        return new Method(type.type, m, args);
    }

    public Stream<Method> staticFactories(ResolvedType type) {
        return type.methods()
                .filter(MethodBinding::isStatic)
                .filter(MethodBinding::isPublic)
                .map(m -> method(type, m));
    }

    public Stream<Method> factories(ResolvedType type) {
        return type.methods()
                .filter(MethodBinding::isPublic)
                .filter(m -> m.parameters.length > 1)
                .map(m -> method(type, m));
    }

    public Stream<Method> inputs(ResolvedType type) {
        return type.methods()
                .filter(MethodBinding::isPublic)
                .filter(m -> m.parameters.length == 1)
                .map(m -> method(type, m));
    }

    public Stream<Method> outputs(ResolvedType type) {
        return type.methods()
                .filter(MethodBinding::isPublic)
                .filter(m -> m.parameters.length == 0)
                .map(m -> method(type, m));
    }

    public boolean isAssignable(ResolvedType from, ResolvedType to) {
        var grounded = ground(to.type);
        return from.type.isBoxingCompatibleWith(grounded, scope);
    }

    private TypeBinding ground(TypeBinding b) {
        if (b instanceof TypeVariableBinding t) {
            return convertEliminatingTypeVariables(t, t, t.rank, null);
        } else if (b instanceof ArrayBinding t) {
            return new ArrayBinding(ground(t.leafComponentType), t.dimensions, t.environment());
        } else if (b instanceof BaseTypeBinding) {
            return b;
        } else if (b instanceof ParameterizedTypeBinding t) {
            return new ParameterizedTypeBinding(
                    t.actualType(),
                    stream(t.arguments).map(this::ground).toArray(TypeBinding[]::new),
                    t.enclosingType(),
                    t.environment()
            );
        } else if (b instanceof IntersectionTypeBinding18 t) {
            return new IntersectionTypeBinding18(
                    stream(t.intersectingTypes).map(e -> (ReferenceBinding) ground(e)).toArray(ReferenceBinding[]::new),
                    scope.environment
            );
        } else if (b instanceof WildcardBinding t) {
            return new WildcardBinding(
                    t.genericType,
                    t.rank,
                    t.bound == null ? null : ground(t.bound),
                    t.otherBounds == null ? null : stream(t.otherBounds).map(this::ground).toArray(TypeBinding[]::new),
                    t.boundKind,
                    scope.environment
            );
        } else {
            return b;
        }
    }

    public List<String> getErrors(String name) {
        return errors.getOrDefault(name, emptyList());
    }

    public List<String> getWarnings(String name) {
        return warns.getOrDefault(name, emptyList());
    }

    public List<String> getInfos(String name) {
        return infos.getOrDefault(name, emptyList());
    }

    public List<String> getMessages(String name) {
        return Stream.of(getErrors(name), getWarnings(name), getInfos(name)).flatMap(List::stream).toList();
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return !warns.isEmpty();
    }

    public boolean hasInfos() {
        return !infos.isEmpty();
    }

    public void forEachError(BiConsumer<String, List<String>> consumer) {
        errors.forEach(consumer);
    }

    public void forEachWarning(BiConsumer<String, List<String>> consumer) {
        warns.forEach(consumer);
    }

    public void forEachInfo(BiConsumer<String, List<String>> consumer) {
        infos.forEach(consumer);
    }

    static List<String> add(List<String> l, String e) {
        if (l == null) {
            return List.of(e);
        }
        return switch (l.size()) {
            case 1 -> List.of(l.get(0), e);
            case 2 -> List.of(l.get(0), l.get(1), e);
            case 3 -> List.of(l.get(0), l.get(1), l.get(2), e);
            case 4 -> List.of(l.get(0), l.get(1), l.get(2), l.get(3), e);
            default -> Stream.concat(l.stream(), Stream.of(e)).toList();
        };
    }

    @Override
    public String toString() {
        return "Results(types=%s,errors=%s,warns=%s,infos=%s)".formatted(types, errors, warns, infos);
    }
}
