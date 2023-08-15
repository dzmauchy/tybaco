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

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.tybaco.types.model.Type;
import org.tybaco.types.resolver.Method.Arg;

import java.util.List;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.IntStream.range;
import static org.eclipse.jdt.core.dom.Modifier.PUBLIC;
import static org.eclipse.jdt.core.dom.Modifier.STATIC;
import static org.eclipse.jdt.core.dom.Modifier.isPublic;
import static org.eclipse.jdt.core.dom.Modifier.isStatic;
import static org.tybaco.types.model.Primitive.VOID;

public final class TypeResolverResults {

    final TreeMap<String, ITypeBinding> types = new TreeMap<>();
    final TreeMap<String, List<String>> errors = new TreeMap<>();
    final TreeMap<String, List<String>> warns = new TreeMap<>();
    final TreeMap<String, List<String>> infos = new TreeMap<>();

    TypeResolverResults() {
    }

    public Type getType(String name) {
        return type(types.get(name));
    }

    public Stream<Method> staticFactories(String name) {
        var type = types.get(name);
        if (type == null) {
            return Stream.empty();
        }
        return stream(type.getDeclaredMethods())
                .filter(m -> !m.isConstructor() && (m.getModifiers() & (PUBLIC | STATIC)) != 0)
                .map(m -> {
                    var types = m.getParameterTypes();
                    var args = range(0, types.length).mapToObj(i -> new Arg(types[i], i)).toList();
                    return new Method(type, name, m, args);
                });
    }

    public Stream<Method> factories(String name) {
        var type = types.get(name);
        if (type == null) {
            return Stream.empty();
        }
        return stream(type.getDeclaredMethods())
                .filter(m -> !m.isConstructor() && isPublic(m.getModifiers()) && !isStatic(m.getModifiers()))
                .flatMap(m -> {
                    var types = m.getParameterTypes();
                    if (types.length < 2) {
                        return Stream.empty();
                    }
                    var args = range(0, types.length).mapToObj(i -> new Arg(types[i], i)).toList();
                    return Stream.of(new Method(type, name, m, args));
                });
    }

    public Stream<Method> inputs(String name) {
        var type = types.get(name);
        if (type == null) {
            return Stream.empty();
        }
        return stream(type.getDeclaredMethods())
                .filter(m -> !m.isConstructor() && isPublic(m.getModifiers()) && !isStatic(m.getModifiers()))
                .flatMap(m -> {
                    var types = m.getParameterTypes();
                    if (types.length != 1) {
                        return Stream.empty();
                    }
                    var args = singletonList(new Arg(types[0], 0));
                    return Stream.of(new Method(type, name, m, args));
                });
    }

    public Stream<Method> outputs(String name) {
        var type = types.get(name);
        if (type == null) {
            return Stream.empty();
        }
        return stream(type.getDeclaredMethods())
                .filter(m -> !m.isConstructor() && isPublic(m.getModifiers()) && !isStatic(m.getModifiers()))
                .flatMap(m -> {
                    if (m.getParameterTypes().length != 0) {
                        return Stream.empty();
                    }
                    return Stream.of(new Method(type, name, m, emptyList()));
                });
    }

    public boolean isAssignmentCompatibleF(String name, Arg arg) {
        var type = types.get(name);
        return type != null && type.isAssignmentCompatible(arg.type);
    }

    public boolean isAssignmentCompatibleB(String name, Arg arg) {
        var type = types.get(name);
        return type != null && arg.type.isAssignmentCompatible(type);
    }

    public boolean isSubtypeCompatibleF(String name, Arg arg) {
        var type = types.get(name);
        return type != null && type.isSubTypeCompatible(arg.type);
    }

    public boolean isSubtypeCompatibleB(String name, Arg arg) {
        var type = types.get(name);
        return type != null && arg.type.isSubTypeCompatible(type);
    }

    private Type type(ITypeBinding type) {
        return type == null ? VOID : Types.from(type);
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

    @Override
    public String toString() {
        return "Results(types=%s,errors=%s,warns=%s,infos=%s)".formatted(types, errors, warns, infos);
    }
}
