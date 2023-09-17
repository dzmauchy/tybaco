package org.tybaco.meta;

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

import org.tybaco.xml.Xml;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.util.List;

public record Library(Meta meta, List<LibraryBlocks> blocks, List<LibraryConstants> constants, List<LibraryType> types) {

  public Library(Element element) {
    this(
      new Meta(element),
      Xml.elementsByTag(element, "blocks").map(LibraryBlocks::new).toList(),
      Xml.elementsByTag(element, "constants").map(LibraryConstants::new).toList(),
      Xml.elementsByTag(element, "type").map(LibraryType::new).toList()
    );
  }

  public static Schema schema() {
    var schemaFactory = SchemaFactory.newDefaultInstance();
    var classLoader = Thread.currentThread().getContextClassLoader();
    try {
      return schemaFactory.newSchema(classLoader.getResource("tybaco/library/library.xsd"));
    } catch (SAXException e) {
      throw new IllegalStateException(e);
    }
  }
}
