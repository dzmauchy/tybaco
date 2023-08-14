package org.montoni.types.resolver;

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

import org.montoni.types.model.Type;

import java.util.List;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.montoni.types.model.Primitive.VOID;

public final class TypeResolverResults {

    final TreeMap<String, Type> types = new TreeMap<>();
    final TreeMap<String, List<String>> errors = new TreeMap<>();
    final TreeMap<String, List<String>> warns = new TreeMap<>();
    final TreeMap<String, List<String>> infos = new TreeMap<>();

    TypeResolverResults() {
    }

    public Type getType(String name) throws TypeResolverException {
        var problem = errors.get(name);
        if (problem == null) {
            return types.getOrDefault(name, VOID);
        } else {
            throw new TypeResolverException(name, problem, types.getOrDefault(name, VOID));
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

    @Override
    public String toString() {
        return "Results(types=%s,errors=%s,warns=%s,infos=%s)".formatted(types, errors, warns, infos);
    }
}
