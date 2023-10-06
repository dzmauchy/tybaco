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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.tybaco.testing.eventually.Eventually;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.tybaco.testing.json.JsonStream.objectList;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoggingServiceProviderTest implements Eventually {

  @Test
  void logSimple() throws Exception {
    var os = new ByteArrayOutputStream();
    try (var provider = new LoggingServiceProvider(os)) {
      var loggerFactory = provider.getLoggerFactory();
      var logger = loggerFactory.getLogger("abc");
      var mdc = provider.getMDCAdapter();
      mdc.put("a", "1");
      logger.info("Hello");
      logger.info("Hola");
      logger.info("Hello {}", "World", new IllegalStateException("s", new IllegalArgumentException("abc")));
      try {
        var elements = eventually(() -> {
          var l = objectList(os);
          assertEquals(3, l.size());
          return l;
        });
        assertEquals(3, elements.size());
      } finally {
        os.writeTo(System.out);
      }
    }
  }
}
