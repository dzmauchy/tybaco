package org.tybaco.types.calc;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Type;
import java.util.List;
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
      arguments(v(C1.class.getDeclaredMethod("m", CharSequence.class), 0), Integer.class, false)
    );
  }

  private static class C1<X> {

    private static <E extends CharSequence> E m(E arg) {
      return null;
    }
  }
}
