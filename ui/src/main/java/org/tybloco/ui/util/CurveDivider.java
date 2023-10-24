package org.tybloco.ui.util;

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
import java.awt.geom.Rectangle2D;

public final class CurveDivider {

  final double[] array;

  public CurveDivider(int steps) {
    array = new double[8 << steps];
  }

  public void divide(double x1, double y1, double cx1, double cy1, double cx2, double cy2, double x2, double y2) {
    array[0] = x1;
    array[1] = y1;
    array[2] = cx1;
    array[3] = cy1;
    array[4] = cx2;
    array[5] = cy2;
    array[6] = x2;
    array[7] = y2;
    subDivide(0, array.length);
  }

  public boolean intersects(Rectangle2D rect) {
    var array = this.array;
    for (int i = 0, l = array.length; i < l; i += 8) {
      if (rect.intersectsLine(array[i], array[i + 1], array[i + 6], array[i + 7])) {
        return true;
      }
    }
    return false;
  }

  public double length() {
    double len = 0d;
    var a = array;
    for (int i = 0, l = a.length; i < l; i += 8) {
      len += Math.hypot(a[0] - a[6], a[1] - a[7]);
    }
    return len;
  }

  private void subDivide(int offset, int size) {
    if ((size >>>= 1) >= 8) {
      var newOffset = offset + size;
      CubicCurve2D.subdivide(array, offset, array, offset, array, newOffset);
      subDivide(offset, size);
      subDivide(newOffset, size);
    }
  }
}
