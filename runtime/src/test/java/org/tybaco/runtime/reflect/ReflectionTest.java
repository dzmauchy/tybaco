package org.tybaco.runtime.reflect;

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

import org.junit.jupiter.api.Test;
import org.tybaco.runtime.basic.CanBeStarted;

import java.lang.invoke.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.tybaco.runtime.reflect.Reflection.lambda;

class ReflectionTest {

  @Test
  void lambdaTest() throws InterruptedException {
    var flag = new AtomicBoolean();
    var thread = new Thread(() -> flag.set(true));
    var lambdaOpt = lambda(CanBeStarted.class, thread, "start", void.class);
    assertThat(lambdaOpt).isPresent();
    lambdaOpt.ifPresent(CanBeStarted::start);
    thread.join();
    assertThat(flag.get()).isTrue();
  }

  @SuppressWarnings("unchecked")
  @Test
  void docLambdaTest() throws Throwable {
    var toBeTrimmed = " text with spaces ";
    var reflectionMethod = String.class.getMethod("trim");
    var lookup = MethodHandles.lookup();
    var handle = lookup.unreflect(reflectionMethod);
    var callSite = LambdaMetafactory.metafactory(
      lookup,
      "get",
      MethodType.methodType(Supplier.class, String.class),
      MethodType.methodType(Object.class),
      handle,
      MethodType.methodType(String.class));
    var lambda = (Supplier<String>) callSite.getTarget().bindTo(toBeTrimmed).invoke();
    var result = lambda.get();
  }
}
