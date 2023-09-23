package org.tybaco.runtime.util;

/*-
 * #%L
 * runtime
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class FListTest {

  @ParameterizedTest
  @MethodSource
  void test(List<String> original) {
    var l = new FList<String>();
    original.forEach(l::add);
    var actual = new ArrayList<String>();
    l.pollEach(actual::add);
    assertEquals(original, actual);
    assertTrue(l.isEmpty());
  }

  static Stream<Arguments> test() {
    return Stream.of(
      arguments(List.of()),
      arguments(List.of("a")),
      arguments(List.of("a", "b")),
      arguments(List.of("a", "b", "c"))
    );
  }
}
