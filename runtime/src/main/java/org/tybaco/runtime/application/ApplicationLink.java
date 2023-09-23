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

import java.util.NoSuchElementException;

import static org.tybaco.runtime.util.Xml.elementByTag;

public record ApplicationLink(ApplicationConnector out, ApplicationConnector in, boolean arg) {

  public ApplicationLink(ApplicationConnector out, ApplicationConnector in) {
    this(out, in, true);
  }

  public ApplicationLink(Element element) {
    this(conn(element, "out"), conn(element, "in"), arg(element));
  }

  private static ApplicationConnector conn(Element e, String tag) {
    return elementByTag(e, tag).map(ApplicationConnector::new).orElseThrow(() -> new NoSuchElementException(tag));
  }

  private static boolean arg(Element e) {
    return switch (e.getAttribute("arg")) {
      case "", "true" -> true;
      default -> false;
    };
  }
}
