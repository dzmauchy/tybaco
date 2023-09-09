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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.Buffer;
import java.nio.CharBuffer;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.tybaco.types.calc.Types.*;

class TypeCalculatorTest {

  @ParameterizedTest
  @MethodSource
  void compatibility(Type formal, Type actual, boolean expected) {
    assertEquals(expected, TypeCalculator.isCompatible(formal, actual));
  }

  static Stream<Arguments> compatibility() throws ReflectiveOperationException {
    return Stream.of(
      arguments(int.class, long.class, false),
      arguments(long.class, int.class, true),
      arguments(p(List.class, String.class), p(List.class, String.class), true),
      arguments(p(List.class, CharSequence.class), p(List.class, String.class), false),
      arguments(p(List.class, wu(CharSequence.class)), p(List.class, String.class), true),
      arguments(p(List.class, wl(CharSequence.class)), p(List.class, String.class), false),
      arguments(p(List.class, wl(CharSequence.class)), p(List.class, Object.class), true),
      arguments(p(List.class, v(List.class, 0)), p(List.class, Object.class), true),
      arguments(v(C1.class.getDeclaredMethod("m", CharSequence.class), 0), String.class, true),
      arguments(v(C1.class.getDeclaredMethod("m", CharSequence.class), 0), CharSequence.class, true),
      arguments(v(C1.class.getDeclaredMethod("m", CharSequence.class), 0), Integer.class, false),
      arguments(p(List.class, wu(CharSequence.class, Serializable.class)), p(List.class, String.class), true),
      arguments(p(List.class, wu(CharSequence.class, Serializable.class)), p(List.class, CharBuffer.class), false),
      arguments(p(List.class, wl(CharSequence.class, Serializable.class)), p(List.class, String.class), false),
      arguments(p(List.class, wl(CharSequence.class, Serializable.class)), p(List.class, Object.class), true),
      arguments(p(List.class, wl(CharBuffer.class)), p(List.class, CharSequence.class), true),
      arguments(p(List.class, wl(CharBuffer.class)), p(List.class, Buffer.class), true),
      arguments(p(List.class, wl(wu(CharBuffer.class))), p(List.class, CharSequence.class), true),
      arguments(p(List.class, wl(wu(CharSequence.class, Serializable.class))), p(List.class, Object.class), true),
      arguments(p(List.class, wu(CharSequence.class)), p(List.class, u(CharBuffer.class, String.class)), true),
      arguments(p(List.class, wu(CharSequence.class)), p(List.class, u(CharBuffer.class, Object.class)), false),
      arguments(p(List.class, String.class), p(ArrayList.class, String.class), true),
      arguments(p(List.class, p(List.class, String.class)), p(List.class, p(ArrayList.class, String.class)), false),
      arguments(p(List.class, wu(p(List.class, String.class))), p(List.class, p(ArrayList.class, String.class)), true),
      arguments(p(List.class, wl(p(ArrayList.class, String.class))), p(List.class, p(AbstractList.class, String.class)), true),
      arguments(p(List.class, wl(p(ArrayList.class, String.class))), p(List.class, p(List.class, String.class)), true),
      arguments(p(List.class, wl(p(ArrayList.class, String.class))), p(List.class, Serializable.class), true)
    );
  }

  private static class C1<X> {

    private static <E extends CharSequence> E m(E arg) {
      return null;
    }
  }
}
