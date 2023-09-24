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

import static java.lang.Integer.parseInt;

public record ApplicationConnector(int block, String spot, int index) {

  public ApplicationConnector(Element element) {
    this(
      parseInt(element.getAttribute("block")),
      element.getAttribute("spot"),
      parseInt(element.getAttribute("index"))
    );
  }

  public static ApplicationConnector out(int block) {
    return new ApplicationConnector(block, "*", -1);
  }

  public static ApplicationConnector out(int block, String spot) {
    return new ApplicationConnector(block, spot, -1);
  }

  public static ApplicationConnector in(int block, String spot) {
    return new ApplicationConnector(block, spot, -1);
  }

  public static ApplicationConnector in(int block, String spot, int index) {
    return new ApplicationConnector(block, spot, index);
  }
}
