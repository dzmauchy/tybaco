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

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.util.List;

import static org.tybaco.runtime.util.Xml.elementsByTag;

public record Application(String id, List<ApplicationConstant> constants, List<ApplicationBlock> blocks, List<ApplicationLink> links) {

  public Application(Element element) {
    this(
      element.getAttribute("id"),
      elementsByTag(element, "constant").map(ApplicationConstant::new).toList(),
      elementsByTag(element, "block").map(ApplicationBlock::new).toList(),
      elementsByTag(element, "link").map(ApplicationLink::new).toList()
    );
  }

  public int maxInternalId() {
    return Math.max(
      constants.stream().mapToInt(ApplicationConstant::id).max().orElse(0),
      blocks.stream().mapToInt(ApplicationBlock::id).max().orElse(0)
    );
  }

  public static Schema schema() {
    var schemaFactory = SchemaFactory.newDefaultInstance();
    var classLoader = Thread.currentThread().getContextClassLoader();
    try {
      return schemaFactory.newSchema(classLoader.getResource("tybaco/application/application.xsd"));
    } catch (SAXException e) {
      throw new IllegalStateException(e);
    }
  }
}
