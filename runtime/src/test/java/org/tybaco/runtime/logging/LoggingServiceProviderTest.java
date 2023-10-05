package org.tybaco.runtime.logging;

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

import org.junit.jupiter.api.*;
import org.tybaco.testing.eventually.Eventually;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.tybaco.testing.json.JsonStream.objectList;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoggingServiceProviderTest implements Eventually {

  private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
  private final LoggingServiceProvider serviceProvider = new LoggingServiceProvider(outputStream, 1 << 24, 64);

  @Test
  void logSimple() throws Exception {
    var loggerFactory = serviceProvider.getLoggerFactory();
    var logger = loggerFactory.getLogger("abc");
    logger.info("Hello");
    logger.info("Hola");
    try {
      var elements = eventually(() -> objectList(outputStream));
      assertEquals(2, elements.size());
    } finally {
      outputStream.writeTo(System.out);
    }
  }

  @BeforeAll
  void beforeAll() {
    serviceProvider.initialize();
  }

  @AfterAll
  void afterAll() {
    serviceProvider.close();
  }
}
