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

import org.tybaco.runtime.util.Xml;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.validation.Schema;
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

  public static Schema schema() throws SAXException {
    return Xml.loadSchema("tybaco/application/application.xsd");
  }
}
