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
import lombok.extern.java.Log;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.WildcardBinding;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static org.eclipse.jdt.internal.compiler.lookup.Scope.convertEliminatingTypeVariables;

@Log(topic = "ResolvedTypes")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class ResolvedTypes {

    private final CompilationUnitDeclaration cu;

    final ConcurrentSkipListMap<String, TypeBinding> types = new ConcurrentSkipListMap<>();
    final ConcurrentSkipListMap<String, List<String>> errors = new ConcurrentSkipListMap<>();
    final ConcurrentSkipListMap<String, List<String>> warns = new ConcurrentSkipListMap<>();
    final ConcurrentSkipListMap<String, List<String>> infos = new ConcurrentSkipListMap<>();

    public ResolvedType getType(String name) {
        var type = types.get(name);
        return new ResolvedType(type == null ? TypeBinding.VOID : type);
    }

    public ResolvedType typeOf(String... parts) {
        var compoundName = stream(parts).map(String::toCharArray).toArray(char[][]::new);
        return new ResolvedType(cu.scope.getType(compoundName, parts.length));
    }

    public ResolvedType array(ResolvedType type, int dimension) {
        return new ResolvedType(cu.scope.createArrayType(type.type, dimension));
    }

    public ResolvedType boxed(ResolvedType type) {
        return new ResolvedType(cu.scope.boxing(type.type));
    }

    public boolean isAssignable(ResolvedType from, ResolvedType to) {
        var grounded = ground(to.type);
        return from.type.isBoxingCompatibleWith(grounded, cu.scope);
    }

    private TypeBinding ground(TypeBinding b) {
        if (b instanceof TypeVariableBinding t) return convertEliminatingTypeVariables(t, t, t.rank, null);
        else if (b instanceof WildcardBinding w) return convertEliminatingTypeVariables(w, w.genericType, w.rank, null);
        else if (b instanceof ReferenceBinding r) return convertEliminatingTypeVariables(r, r, 0, null);
        else return convertEliminatingTypeVariables(b, null, 0, null);
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

    public void forEachType(BiConsumer<String, ResolvedType> consumer) {
        types.forEach((name, b) -> consumer.accept(name, new ResolvedType(b)));
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
        return l == null ? List.of(e) : Stream.concat(l.stream(), Stream.of(e)).toList();
    }

    @Override
    public String toString() {
        return "Results(types=%s,errors=%s,warns=%s,infos=%s)".formatted(types, errors, warns, infos);
    }
}
