package org.tybaco.runtime.application;

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
import org.tybaco.runtime.application.beans.SampleBeanA;
import org.tybaco.runtime.application.beans.SampleBeanB;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tybaco.runtime.application.Connector.out;

class ApplicationRunnerTest {

  @Test
  void a() throws Exception {
    // given
    var blocks = List.of(Block.fromMethod(0, "b1", SampleBeanA.class.getMethod("sampleBeanA")));
    var app = new Application("app", "App", blocks, List.of());
    // when
    runApp(app);
    // then
    assertTrue(SampleBeanA.closed);
    assertTrue(SampleBeanA.started);
  }

  @Test
  void b() throws Exception {
    // given
    var blocks = List.of(
      new Block(0, "c1", "int", "12"),
      new Block(1, "c2", "long", "234"),
      new Block(2, "c3", "URI", "http://localhost:80"),
      new Block(3, "c4", BigDecimal.class.getName(), "1.2"),
      Block.fromMethod(100, "b1", SampleBeanB.class.getMethod("sampleBeanB", Object[].class))
    );
    var links = List.of(
      new Link(out(0), new Connector(100, "+v", 1)),
      new Link(out(1), new Connector(100, "+v", 3)),
      new Link(out(2), new Connector(100, "+v", 4)),
      new Link(out(3), new Connector(100, "values", 2))
    );
    var app = new Application("app", "App", blocks, links);
    // when
    runApp(app);
    // then
    assertArrayEquals(new Object[] {null, 12, null, 234L, new URI("http://localhost:80")}, SampleBeanB.values);
    assertArrayEquals(new Object[] {null, null, new BigDecimal("1.2")}, SampleBeanB.constructorValues);
  }

  private void runApp(Application app) {
    var runner = new ApplicationRunner();
    try (var runtime = runner.runtimeApp(app)) {
      runtime.run();
    }
  }
}
