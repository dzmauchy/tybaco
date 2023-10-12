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

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tybaco.ui.child.project.diagram.DiagramBlockInput;
import org.tybaco.ui.child.project.diagram.DiagramBlockOutput;
import org.w3c.dom.Element;

import java.util.NoSuchElementException;

import static org.tybaco.xml.Xml.elementByTag;
import static org.tybaco.xml.Xml.withChild;

public final class Link {

  public final Connector out;
  public final Connector in;
  public final int index;
  public final SimpleObjectProperty<DiagramBlockOutput> output = new SimpleObjectProperty<>(this, "output");
  public final SimpleObjectProperty<DiagramBlockInput> input = new SimpleObjectProperty<>(this, "input");
  public final SimpleBooleanProperty separated = new SimpleBooleanProperty(this, "separated");
  public final BooleanBinding lineEnabled = output.isNotNull().and(input.isNotNull());

  public Link(Connector out, Connector in, int index) {
    this.out = out;
    this.in = in;
    this.index = index;
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
