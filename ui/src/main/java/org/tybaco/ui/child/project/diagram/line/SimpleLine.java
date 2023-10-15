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
    if (xe - xs <= 30d)
      return false;
    double ys = ob.getCenterY(), ye = ib.getCenterY();
    double w = (xe - xs) / 5d;
    if (r1(xs, ys, xe, ye, xs + w, xe - w))
      return true;
    if (l2(xs, ys, xe, ye, xs + w, xe - w))
      return true;
    return false;
  }

  private boolean r1(double xs, double ys, double xe, double ye, double cx1, double cx2) {
    if (cx2 - cx1 <= STEP) return false;
    return line.tryApply(D4, xs + SAFE_DIST, ys, cx1, ys, cx2, ye, xe - SAFE_DIST, ye) || r1(xs, ys, xe, ye, cx1 + STEP, cx2);
  }

  private boolean l2(double xs, double ys, double xe, double ye, double cx1, double cx2) {
    if (cx2 - cx1 <= STEP) return false;
    return line.tryApply(D4, xs + SAFE_DIST, ys, cx1, ys, cx2, ye, xe - SAFE_DIST, ye) || l2(xs, ys, xe, ye, cx1, cx2 - STEP);
  }
}
