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

public final class Connector {

  public final int blockId;
  public final String spot;
  public final SimpleDoubleProperty x = new SimpleDoubleProperty(this, "x", 0d);
  public final SimpleDoubleProperty y = new SimpleDoubleProperty(this, "y", 0d);

  public Connector(int blockId, String spot) {
    this.blockId = blockId;
    this.spot = spot;
  }

  public Connector(Element element) {
    this(Integer.parseInt(element.getAttribute("block")), element.getAttribute("spot"));
  }

  public void saveTo(Element element) {
    element.setAttribute("block", Integer.toString(blockId));
    element.setAttribute("spot", spot);
  }

  @Override
  public int hashCode() {
    return blockId ^ spot.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Connector c && blockId == c.blockId && spot.equals(c.spot);
  }

  @Override
  public String toString() {
    return blockId + "[" + spot + "]";
  }
}
