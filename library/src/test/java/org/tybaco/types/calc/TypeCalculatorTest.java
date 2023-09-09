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

import com.google.common.collect.Iterables;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.Buffer;
import java.nio.CharBuffer;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.tybaco.types.calc.TypeCalculator.isCompatible;
import static org.tybaco.types.calc.Types.*;

class TypeCalculatorTest {

  @ParameterizedTest
  @MethodSource
  void compatibility(Type formal, Type actual, boolean expected) {
    assertEquals(expected, isCompatible(formal, actual));
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
      arguments(v(C1.class.getMethod("m", CharSequence.class), 0), String.class, true),
      arguments(v(C1.class.getMethod("m", CharSequence.class), 0), CharSequence.class, true),
      arguments(v(C1.class.getMethod("m", CharSequence.class), 0), Integer.class, false),
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

  @ParameterizedTest
  @MethodSource
  void resolveReturnType(Method method, Map<String, Type> args, Type expectedReturnType, Map<String, Boolean> compatibility) {
    var calc = new TypeCalculator(method, args);
    var returnType = calc.outputType();
    assertEquals(expectedReturnType, returnType);
    compatibility.forEach((name, expected) -> {
      var actual = calc.isCompatible(name);
      assertEquals(expected, actual);
    });
  }

  static Stream<Arguments> resolveReturnType() throws ReflectiveOperationException {
    return Stream.of(
      arguments(
        C1.class.getMethod("m", CharSequence.class),
        Map.of("arg", String.class),
        String.class,
        Map.of("arg", true)
      ),
      arguments(
        C1.class.getMethod("m", CharSequence.class),
        Map.of("arg", Object.class),
        wu(CharSequence.class),
        Map.of("arg", false)
      ),
      arguments(
        C1.class.getMethod("m", CharSequence.class),
        Map.of("arg", u(String.class, CharBuffer.class)),
        u(String.class, CharBuffer.class),
        Map.of("arg", true)
      ),
      arguments(
        C1.class.getMethod("m2", Object.class),
        Map.of("x", Integer.class),
        po(TypeCalculatorTest.class, C1.class, p(List.class, wu(Integer.class))),
        Map.of("x", true)
      ),
      arguments(
        C1.class.getMethod("m3", CharSequence[].class),
        Map.of("args", String.class),
        po(TypeCalculatorTest.class, C1.class, p(List.class, String.class)),
        Map.of("args", true)
      ),
      arguments(
        C1.class.getMethod("m3", CharSequence[].class),
        Map.of("args", va(String.class, CharBuffer.class)),
        po(TypeCalculatorTest.class, C1.class, p(List.class, u(String.class, CharBuffer.class))),
        Map.of("args", true)
      ),
      arguments(
        C2.class.getMethod("cons", Consumer.class),
        Map.of("consumer", p(Consumer.class, Integer.class)),
        Integer.class,
        Map.of("consumer", true)
      ),
      arguments(
        C2.class.getMethod("cons2", Consumer.class),
        Map.of("v", p(Consumer.class, p(List.class, Object.class))),
        Object.class,
        Map.of("v", true)
      ),
      arguments(
        C2.class.getMethod("cons2", Consumer.class),
        Map.of("v", p(Consumer.class, p(Collection.class, Object.class))),
        Object.class,
        Map.of("v", true)
      ),
      arguments(
        C2.class.getMethod("cons2", Consumer.class),
        Map.of("v", p(Consumer.class, p(ArrayList.class, Object.class))),
        wu(),
        Map.of("v", false)
      )
    );
  }

  @ParameterizedTest
  @MethodSource
  void resolveOutputType(Method method, Map<String, Type> args, String out, Type expected) {
    var calc = new TypeCalculator(method, args);
    var outputType = calc.outputType(out).orElse(null);
    assertEquals(expected, outputType);
  }

  static Stream<Arguments> resolveOutputType() throws ReflectiveOperationException {
    return Stream.of(
      arguments(
        C1.class.getMethod("m2", Object.class),
        Map.of("x", Void.class),
        "x",
        p(List.class, wu(Void.class))
      ),
      arguments(
        C2.class.getMethod("h", Object.class),
        Map.of("x", Integer.class),
        "x",
        Integer.class
      ),
      arguments(
        C2.class.getMethod("h", Object.class),
        Map.of("x", Integer.class),
        "u",
        null
      )
    );
  }

  public static class C1<X> {

    public static <E extends CharSequence> E m(E arg) {
      return null;
    }

    public static <V> C1<List<? extends V>> m2(V x) {
      return null;
    }

    @SafeVarargs
    public static <E extends CharSequence> C1<List<E>> m3(E... args) {
      return null;
    }

    public X x() {
      return null;
    }
  }

  public static class C2<Y> extends C1<Y> {

    public static <C> C2<C> h(C x) {
      return null;
    }

    public static <X> X cons(Consumer<? super X> consumer) {
      return null;
    }

    public static <X> X cons2(Consumer<? super List<? extends X>> v) {
      return null;
    }
  }
}
