package org.tybaco.ui.main.projects;

/*-
 * #%L
 * ui
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

import static org.tybaco.xml.Xml.elementByTag;
import static org.tybaco.xml.Xml.withChild;

public record Link(Connector out, Connector in) {

  public static Link loadFrom(Element element) {
    return new Link(
      Connector.loadFrom(elementByTag(element, "out")),
      Connector.loadFrom(elementByTag(element, "in"))
    );
  }

  public void saveTo(Element element) {
    withChild(element, "out", out::saveTo);
    withChild(element, "in", in::saveTo);
  }
}
