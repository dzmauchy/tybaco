package org.tybaco.types;

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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.tybaco.types.model.Atomic;
import org.tybaco.types.model.Type;
import org.tybaco.types.resolver.TypeResolver;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.tybaco.types.model.Primitive.DOUBLE;
import static org.tybaco.types.model.Primitive.FLOAT;
import static org.tybaco.types.model.Primitive.INT;
import static org.tybaco.types.model.Primitive.LONG;
import static org.tybaco.types.model.Primitive.VOID;
import static org.tybaco.types.resolver.CommonTypes.BOXED_INT;
import static org.tybaco.types.resolver.CommonTypes.BOXED_LONG;
import static org.tybaco.types.resolver.CommonTypes.listOf;
import static org.tybaco.types.resolver.CommonTypes.mapOf;

class TypeResolverTest {

    private final TypeResolver resolver = new TypeResolver("Test", new String[0], new String[0]);

    @ParameterizedTest
    @MethodSource
    void simpleTypes(String expr, Type expected) {
        var map = resolver.resolve(Map.of("a", expr));
        var actual = assertDoesNotThrow(() -> map.getType("a"));
        assertEquals(expected, actual);
    }

    static Stream<Arguments> simpleTypes() {
        return Stream.of(
                arguments("1 + 10", INT),
                arguments("1L + 19", LONG),
                arguments("1d + 23", DOUBLE),
                arguments("1f + 23", FLOAT),
                arguments("\"abc\"", new Atomic("java.lang.String")),
                arguments("(Integer) 1", new Atomic("java.lang.Integer")),
                arguments("2", INT)
        );
    }

    @ParameterizedTest
    @MethodSource
    void parameterizedTypes(String expr, Type expected) {
        var map = resolver.resolve(Map.of("a", expr));
        var actual = assertDoesNotThrow(() -> map.getType("a"));
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
        var map = resolver.resolve(Map.of("a", expr));
        assertTrue(map.hasErrors());
        assertFalse(map.getErrors("a").isEmpty());
        assertEquals(expected, map.getType("a"));
    }

    static Stream<Arguments> errorTypes() {
        return Stream.of(
                arguments("System.out.write(new char[0])", VOID)
        );
    }

    @ParameterizedTest
    @MethodSource
    void resolveTypes(String type, boolean hasErrors, Type expected) {
        var map = resolver.resolveTypes(List.of(type));
        assertEquals(hasErrors, map.hasErrors());
        assertEquals(expected, map.getType(type));
    }

    static Stream<Arguments> resolveTypes() {
        return Stream.of(
                arguments("java.util.List", false, new Atomic(List.class.getName())),
                arguments("int", false, INT)
        );
    }

    @Test
    void staticFactories() {
        var mapExpr = resolver.resolve(Map.of("a", "2"));
        var mapTypes = resolver.resolveTypes(List.of("java.util.List"));
        var method = mapTypes.staticFactories("java.util.List")
                .filter(m -> m.getArgs().size() == 1)
                .filter(m -> !m.isVarargs())
                .filter(m -> m.getName().equals("of"))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No method found: List.of(arg)"));
        assertEquals(1, method.getArgs().size());
        assertTrue(mapExpr.isAssignmentCompatibleF("a", method.getArgs().get(0)));
    }
}
