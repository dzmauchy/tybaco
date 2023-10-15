package org.tybaco.ui.child.project.diagram.line;

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

import static org.tybaco.ui.child.project.diagram.line.DiagramLine.*;

final class SimpleLine {

  private final DiagramLine line;

  SimpleLine(DiagramLine line) {
    this.line = line;
  }

  boolean trySimpleLine(Bounds ib, Bounds ob) {
    double xs = ob.getMaxX() + SAFE_DIST, xe = ib.getMinX() - SAFE_DIST;
    if (xe - xs <= 30d) return false;
    double ys = ob.getCenterY(), ye = ib.getCenterY();
    double vs = Math.signum(ye - ys);
    double w = (xe - xs) / 5d;
    for (double cx1 = xs + w, cx2 = xe - w; cx1 <= xe - w; cx1 += STEP) {
      if (tryVertical(xs, xe, ys, ye, vs, cx1, cx2)) return true;
    }
    for (double cx1 = xs + w, cx2 = xe - w; cx2 >= xs + w; cx2 -= STEP) {
      if (tryVertical(xs, xe, ys, ye, vs, cx1, cx2)) return true;
    }
    for (double cx1 = xs + w + STEP, cx2 = xe - w - STEP; cx2 >= xs + w && cx1 <= xe - w; cx1 += STEP, cx2 -= STEP) {
      if (tryVertical(xs, xe, ys, ye, vs, cx1, cx2)) return true;
    }
    return false;
  }

  private boolean tryVertical(double xs, double xe, double ys, double ye, double vs, double cx1, double cx2) {
    for (int i = 0; i < 6; i++) {
      if (line.tryApply(D4, xs, ys, cx1, ys + i * STEP * vs, cx2, ye - i - STEP * vs, xe, ye)) {
        return true;
      }
      if (line.tryApply(D4, xs, ys, cx1, ys - i * STEP * vs, cx2, ye - i + STEP * vs, xe, ye)) {
        return true;
      }
    }
    return false;
  }
}
