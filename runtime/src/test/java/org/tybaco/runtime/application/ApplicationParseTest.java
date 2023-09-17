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
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;
import static org.tybaco.runtime.util.Xml.load;

class ApplicationParseTest {

  @Test
  void parseSuccessFromString() throws Exception {
    // language=xml
    var xml = """
      <project xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="/tybaco/application/application.xsd" id="abc">
        <constant id="23" factory="int" value="45" name="abc"/>
      </project>
      """;
    var inputSource = new InputSource(new StringReader(xml));
    var application = load(inputSource, Application.schema(), Application::new);
    assertEquals(1, application.constants().size());
    assertEquals("abc", application.id());
    assertEquals("int", application.constants().get(0).factory());
    assertEquals("45", application.constants().get(0).value());
    assertEquals(23, application.constants().get(0).id());
  }

  @Test
  void parseFailureFromString() {
    // language=xml
    var xml = """
      <project xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="/tybaco/application/application.xsd" id="abc">
        <constant id="anInvalidId" factory="int" value="45" name="abc"/>
      </project>
      """;
    var inputSource = new InputSource(new StringReader(xml));
    var exception = assertThrows(SAXParseException.class, () -> load(inputSource, Application.schema(), Application::new));
    assertTrue(exception.getMessage().contains("anInvalidId"));
  }
}
