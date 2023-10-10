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

import javafx.beans.property.SimpleDoubleProperty;
import org.w3c.dom.Element;

import java.util.NoSuchElementException;

import static org.tybaco.xml.Xml.elementByTag;
import static org.tybaco.xml.Xml.withChild;

public final class Link {

  public final Connector out;
  public final Connector in;
  public final int index;
  public final SimpleDoubleProperty outX = new SimpleDoubleProperty(this, "outX", 0d);
  public final SimpleDoubleProperty outY = new SimpleDoubleProperty(this, "outY", 0d);
  public final SimpleDoubleProperty inX = new SimpleDoubleProperty(this, "inX", 0d);
  public final SimpleDoubleProperty inY = new SimpleDoubleProperty(this, "inY", 0d);

  public Link(Connector out, Connector in, int index) {
    this.out = out;
    this.in = in;
    this.index = index;
  }

  public Link(Connector out, Connector in) {
    this(out, in, -1);
  }

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

  @Override
  public int hashCode() {
    return out.hashCode() ^ in.hashCode() ^ index;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Link l && out.equals(l.out) && in.equals(l.in) && index == l.index;
  }

  @Override
  public String toString() {
    return out + " --> " + in + (index >= 0 ? "[" + index + "]" : "");
  }
}
