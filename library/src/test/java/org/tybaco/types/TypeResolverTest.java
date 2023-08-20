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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.tybaco.types.resolver.TypeResolver;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TypeResolverTest {

    private final TypeResolver resolver = new TypeResolver("Test", new String[0]);

    @ParameterizedTest
    @CsvSource({
            "java.util.List,java.util.List<E>",
            "java.util.Map,'java.util.Map<K,V>'"
    })
    void resolveType(String type, String expected) {
        var t = resolver.resolve(type);
        assertEquals(expected, t.toString());
    }

    @ParameterizedTest
    @MethodSource
    void primitiveTypeResolve(String expr, String expected) {
        var results = resolver.resolve(Map.of("a", expr));
        var type = results.getType("a");
        assertTrue(type.isPrimitive());
        assertEquals(expected, type.toString());
    }

    private static Stream<Arguments> primitiveTypeResolve() {
        return Stream.of(
                arguments("1", "int"),
                arguments("2 + 3L", "long"),
                arguments("3d + 4f", "double"),
                arguments("4 == 3", "boolean"),
                arguments("\"a\".toCharArray()[0]", "char")
        );
    }

    @ParameterizedTest
    @MethodSource
    void typeResolve(String expr, String expected) {
        var results = resolver.resolve(Map.of("a", expr));
        var type = results.getType("a");
        assertEquals(expected, type.toString());
    }

    private static Stream<Arguments> typeResolve() {
        return Stream.of(
                arguments("int.class", "java.lang.Class<java.lang.Integer>"),
                arguments("java.util.List.class", "java.lang.Class<java.util.List>")
        );
    }

    @ParameterizedTest
    @MethodSource
    void resolve(Map<String, String> map, Map<String, String> expected) {
        var results = resolver.resolve(map);
        map.forEach((k, v) -> {
            var e = expected.get(k);
            assertNotNull(e, () -> "No key %s in %s".formatted(k, expected));
            var r = results.getType(k);
            assertNotNull(r, () -> "No type found for %s".formatted(k));
            assertEquals(r.toString(), e);
        });
    }

    private static Stream<Arguments> resolve() {
        return Stream.of(
                arguments(
                        linkedMap(
                                entry("a", "3"),
                                entry("b", "java.util.List.of(a)")
                        ),
                        Map.ofEntries(
                                entry("a", "int"),
                                entry("b", "java.util.List<java.lang.Integer>")
                        )
                )
        );
    }

    @Test
    void assignability() {
        var r = resolver.resolve(Map.of("a", "java.util.List.class", "b", "3", "c", "\"\""));

        var a = r.getType("a");
        var b = r.getType("b");
        var c = r.getType("c");
        assertNotNull(a);
        assertNotNull(b);
        assertNotNull(c);

        assertTrue(a.isParameterized());

        var p = a.getTypeParameter(0);
        assertTrue(p.isRaw());
        var pa = p.getActual();
        assertFalse(pa.isRaw());

        var method = p.staticFactories()
                .filter(m -> m.getName().equals("of"))
                .filter(m -> !m.isVarargs())
                .filter(m -> m.getArgs().size() == 1)
                .findFirst()
                .orElseThrow();
        var arg = method.getArgs().get(0);
        var argType = arg.getType();

        assertTrue(r.isAssignable(a, argType));
    }

    @Test
    void complexAssignability() {
        var r = resolver.resolve(Map.of("a", "A.class", "b", "3", "c", "java.time.temporal.ChronoUnit.DAYS"), """
                class A {
                    public static <E extends Enum<E>> java.util.List<E> listOfEnums(E elem) {
                        return java.util.List.of(elem);
                    }
                }
                """);

        var a = r.getType("a");
        var b = r.getType("b");
        var c = r.getType("c");
        assertNotNull(a);
        assertNotNull(b);
        assertNotNull(c);

        assertTrue(a.isParameterized());

        var p = a.getTypeParameter(0);

        var method = p.staticFactories()
                .filter(m -> m.getName().equals("listOfEnums"))
                .findFirst()
                .orElseThrow();
        var arg = method.getArgs().get(0);
        var argType = arg.getType();

        assertFalse(r.isAssignable(a, argType));
        assertFalse(r.isAssignable(b, argType));
        assertTrue(r.isAssignable(c, argType));
    }

    @Test
    void multipleAssignability() {
        var r1 = resolver.resolve(Map.of(
                "a", "(java.util.List<?>) java.util.List.of(1)",
                "b", "java.util.List.of(3)"
        ));
        var r2 = resolver.resolve(Map.of("b", "java.util.List.of(2)"));

        assertTrue(r2.isAssignable(r2.getType("b"), r1.getType("a")));
        assertTrue(r1.isAssignable(r2.getType("b"), r1.getType("a")));
        assertFalse(r2.isAssignable(r1.getType("a"), r2.getType("b")));
        assertFalse(r1.isAssignable(r1.getType("a"), r2.getType("b")));
        assertTrue(r1.isAssignable(r1.getType("b"), r2.getType("b")));
        assertEquals(r1.getType("b"), r2.getType("b"));
    }

    @Test
    void parameterizedType() {
        var t = resolver.parameterizedType(
                resolver.resolve("java.util.List"),
                List.of(resolver.resolve("java.lang.Integer"))
        );
        assertEquals("java.util.List<java.lang.Integer>", t.toString());
    }

    @Test
    void intersectionType() {
        var t = resolver.intersectionType(List.of(
                resolver.resolve("java.lang.Integer"),
                resolver.resolve("java.lang.Number")
        ));
        assertEquals("java.lang.Integer & java.lang.Number", t.toString());
    }

    @SafeVarargs
    private static <K, V> LinkedHashMap<K, V> linkedMap(Map.Entry<K, V>... entries) {
        var map = new LinkedHashMap<K, V>(entries.length);
        for (var entry: entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    @AfterAll
    void afterAll() {
        resolver.close();
    }
}
