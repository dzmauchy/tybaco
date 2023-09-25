package org.tybaco.runtime.application.tasks;

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
import org.tybaco.runtime.application.*;
import org.tybaco.runtime.application.beans.*;
import org.tybaco.runtime.exception.CircularBlockReferenceException;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.tybaco.runtime.helper.ApplicationHelper.*;

class ApplicationRunnerTest {

  @Test
  void simplestBean() throws Exception {
    // given
    var blocks = List.of(ApplicationBlock.fromMethod(0, SampleBeanA.class.getMethod("sampleBeanA")));
    var app = new Application("app", List.of(), blocks, List.of());
    // when
    runApp(app);
    // then
    assertTrue(SampleBeanA.closed);
    assertTrue(SampleBeanA.started);
  }

  @Test
  void aBeanWithConstantsAndOneBlock() throws Exception {
    // given
    var constants = List.of(
      new ApplicationConstant(0, "int", "12"),
      new ApplicationConstant(1, "long", "234"),
      new ApplicationConstant(2, "java.net.URI", "http://localhost:80"),
      new ApplicationConstant(3, "java.math.BigDecimal", "1.2")
    );
    var blocks = List.of(
      ApplicationBlock.fromMethod(32, SampleBeanB.class.getMethod("sampleBeanB", Object[].class))
    );
    var links = List.of(
      inp(out(0), in(32, "v", 1)),
      inp(out(1), in(32, "v", 3)),
      inp(out(2), in(32, "v", 4)),
      arg(out(3), in(32, "values", 2))
    );
    var app = new Application("app", constants, blocks, links);
    // when
    runApp(app);
    // then
    assertArrayEquals(new Object[] {null, null, new BigDecimal("1.2")}, SampleBeanB.constructorValues);
    assertArrayEquals(new Object[] {null, 12, null, 234L, new URI("http://localhost:80")}, SampleBeanB.values);
  }

  @Test
  void circularReference() throws Exception {
    // given
    var constants = List.of(
      new ApplicationConstant(0, "int", "12")
    );
    var blocks = List.of(
      ApplicationBlock.fromConstructor(32, SampleBeanC.class),
      ApplicationBlock.fromMethod(33, SampleBeanB.class.getMethod("sampleBeanB", Object[].class)),
      ApplicationBlock.fromMethod(34, SampleBeanA.class.getMethod("sampleBeanA"))
    );
    var links = List.of(
      arg(out(33, "o"), in(32, "x")),
      arg(out(32), in(33, "values"))
    );
    var app = new Application("app", constants, blocks, links);
    // when
    var exc = assertThrows(RuntimeException.class, () -> runApp(app));
    // then
    assertThat(exc).hasRootCauseInstanceOf(CircularBlockReferenceException.class);
  }

  private void runApp(Application app) {
    var runner = new ApplicationRunner();
    try (var runtime = runner.runtimeApp(app)) {
      assertNotNull(runtime);
    }
  }
}
