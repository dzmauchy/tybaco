package org.montoni.types;

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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.montoni.types.model.Atomic;
import org.montoni.types.model.Type;
import org.montoni.types.resolver.TypeResolver;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.montoni.types.model.Primitive.DOUBLE;
import static org.montoni.types.model.Primitive.FLOAT;
import static org.montoni.types.model.Primitive.INT;
import static org.montoni.types.model.Primitive.LONG;
import static org.montoni.types.model.Primitive.VOID;
import static org.montoni.types.resolver.FrequentTypes.BOXED_INT;
import static org.montoni.types.resolver.FrequentTypes.BOXED_LONG;
import static org.montoni.types.resolver.FrequentTypes.listOf;
import static org.montoni.types.resolver.FrequentTypes.mapOf;

class TypeResolverTest {

    private final TypeResolver resolver = new TypeResolver("Test", new String[0], new String[0]);

    @ParameterizedTest
    @MethodSource
    void simpleTypes(String expr, Type expected) {
        var map = resolver.resolve(List.of(Map.entry("a", expr)));
        var actual = map.get("a");
        assertEquals(expected, actual);
    }

    static Stream<Arguments> simpleTypes() {
        return Stream.of(
                arguments("1 + 10", INT),
                arguments("1L + 19", LONG),
                arguments("1d + 23", DOUBLE),
                arguments("1f + 23", FLOAT),
                arguments("\"abc\"", new Atomic("java.lang.String")),
                arguments("(Integer) 1", new Atomic("java.lang.Integer"))
        );
    }

    @ParameterizedTest
    @MethodSource
    void parameterizedTypes(String expr, Type expected) {
        var map = resolver.resolve(List.of(Map.entry("a", expr)));
        var actual = map.get("a");
        assertEquals(expected, actual);
    }

    static Stream<Arguments> parameterizedTypes() {
        return Stream.of(
                arguments("java.util.Arrays.asList(1)", listOf(BOXED_INT)),
                arguments("java.util.Map.of(1, 2L)", mapOf(BOXED_INT, BOXED_LONG))
        );
    }

    @ParameterizedTest
    @MethodSource
    void errorTypes(String expr, Type expected) {
        var map = resolver.resolve(List.of(Map.entry("a", expr)));
        var actual = map.get("a");
        assertEquals(expected, actual);
    }

    static Stream<Arguments> errorTypes() {
        return Stream.of(
                arguments("System.out.write(new char[0])", VOID)
        );
    }
}
