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

import javafx.geometry.Bounds;
import javafx.scene.shape.CubicCurve;

import java.awt.geom.*;
import java.util.function.Consumer;

public final class ArrayBasedCurveDivider {

  private final double[] array;

  public ArrayBasedCurveDivider(int steps) {
    array = new double[8 << steps];
  }

  public Consumer<CubicCurve> divide(double x1, double y1, double cx1, double cy1, double cx2, double cy2, double x2, double y2) {
    array[0] = x1;
    array[1] = y1;
    array[2] = cx1;
    array[3] = cy1;
    array[4] = cx2;
    array[5] = cy2;
    array[6] = x2;
    array[7] = y2;
    subDivide(0, array.length);
    return c -> setCurve(c, x1, y1, cx1, cy1, cx2, cy2, x2, y2);
  }

  public boolean intersects(Bounds bounds) {
    var array = this.array;
    for (int i = 0; i < array.length; i += 8) {
      var rect = new Rectangle2D.Double(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
      if (rect.intersectsLine(array[i], array[i + 1], array[i + 6], array[i + 7])) {
        return true;
      }
    }
    return false;
  }

  private void subDivide(int offset, int size) {
    if (size == 8) return;
    var newSize = size >> 1;
    var newOffset = offset + newSize;
    CubicCurve2D.subdivide(array, offset, array, offset, array, newOffset);
    subDivide(offset, newSize);
    subDivide(newOffset, newSize);
  }

  public void setCurve(CubicCurve c, double x1, double y1, double cx1, double cy1, double cx2, double cy2, double x2, double y2) {
    c.setStartX(x1);
    c.setStartY(y1);
    c.setControlX1(cx1);
    c.setControlY1(cy1);
    c.setControlX2(cx2);
    c.setControlY2(cy2);
    c.setEndX(x2);
    c.setEndY(y2);
  }
}
