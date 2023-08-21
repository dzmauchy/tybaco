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

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openjdk.jol.info.GraphLayout;
import org.tybaco.types.resolver.TypeResolver;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("manual")
class TypeResolverMemoryLeakTest {

    @Test
    void test() {
        try (var resolver = new TypeResolver("p")) {
            resolver.resolve(Map.of("a", "java.util.List.of(1)"));
            var prevGraph = GraphLayout.parseInstance(resolver);
            long prev = prevGraph.totalSize();
            for (int i = 0; i < 1000; i++) {
                var r = resolver.resolve(Map.of("a", "java.util.List.of(1)"));
                assertNotNull(r);
                long start = System.nanoTime();
                var graph = GraphLayout.parseInstance(resolver);
                long current = graph.totalSize();
                long instances = graph.totalCount();
                long time = System.nanoTime() - start;
                System.out.printf("%d: %d; %d ms; %d%n", i, current - prev, time / 1_000_000L, instances);
                prev = current;
            }
        }
    }
}
