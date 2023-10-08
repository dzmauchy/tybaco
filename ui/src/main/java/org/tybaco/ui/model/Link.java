package org.tybaco.ui.model;

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

import java.util.NoSuchElementException;

import static org.tybaco.xml.Xml.elementByTag;
import static org.tybaco.xml.Xml.withChild;

public record Link(Connector out, Connector in, int index) {

  public Link(Element element) {
    this(
      new Connector(elementByTag(element, "out").orElseThrow(() -> new NoSuchElementException("out"))),
      new Connector(elementByTag(element, "in").orElseThrow(() -> new NoSuchElementException("in"))),
      Integer.parseInt(element.getAttribute("index"))
    );
  }

  public void saveTo(Element element) {
    withChild(element, "out", out::saveTo);
    withChild(element, "in", in::saveTo);
    element.setAttribute("index", Integer.toString(index));
  }

  public boolean inputMatches(Block block, String spot) {
    return in.blockId == block.id && in.spot.equals(spot);
  }

  public boolean outputMatches(Block block, String spot) {
    return out.blockId == block.id && out.spot.equals(spot);
  }
}
