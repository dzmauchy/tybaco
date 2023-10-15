package org.tybaco.ui.util;

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

import java.awt.geom.CubicCurve2D;
import java.util.LinkedList;

public interface CurveDivider {

  static Iterable<CubicCurve2D.Double> divide(CubicCurve2D.Double curve, int steps) {
    var list = new LinkedList<CubicCurve2D.Double>();
    list.add(curve);
    for (int i = 0; i < steps; i++) {
      for (var it = list.listIterator(0); it.hasNext(); ) {
        var c = it.next();
        var c1 = new CubicCurve2D.Double();
        var c2 = new CubicCurve2D.Double();
        c.subdivide(c1, c2);
        it.set(c2);
        it.add(c1);
      }
    }
    return list;
  }
}
