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
@TestMethodOrder(MethodOrderer.MethodName.class)
class LoggingServiceProviderTest implements Eventually {

  @Test
  void log() throws Exception {
    var os = new ByteArrayOutputStream();
    try (var provider = new LoggingServiceProvider(os)) {
      var loggerFactory = provider.getLoggerFactory();
      var logger = loggerFactory.getLogger("abc");
      var mdc = provider.getMDCAdapter();
      var markers = provider.getMarkerFactory();
      mdc.put("a", "1");
      logger.info("Hello");
      logger.info("Hola");
      logger.info("Hello {}", "World", new IllegalStateException("s", new IllegalArgumentException("abc")));
      logger.info(markers.getMarker("a"), "Hi");
      var elements = eventually(() -> {
        var l = objectList(os);
        assertEquals(4, l.size());
        return l;
      });
      assertEquals(4, elements.size());
    }
    os.writeTo(System.out);
  }

  @Test
  void logMultiple() {
    var os = new ByteArrayOutputStream();
    try (var provider = new LoggingServiceProvider(os)) {
      var loggerFactory = provider.getLoggerFactory();
      var logger = loggerFactory.getLogger("abc");
      var mdc = provider.getMDCAdapter();
      var markers = provider.getMarkerFactory();
      mdc.put("a", "1");
      for (int i = 0; i < 1_000; i++) {
        logger.info("Hello");
        logger.info("Hola");
        logger.info("Hello {}", "World");
        logger.info(markers.getMarker("a"), "Hi");
      }
      var elements = eventually(() -> {
        var l = objectList(os);
        assertEquals(4_000, l.size());
        return l;
      });
      assertEquals(4_000, elements.size());
    }
  }
}
