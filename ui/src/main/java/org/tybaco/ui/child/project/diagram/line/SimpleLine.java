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

final class SimpleLine {

  private static final double STEP = 29d;

  static boolean sl(DiagramLine line, double xs, double ys, double xe, double ye) {
    if (xe - xs <= 30d) return true;
    double vs = Math.signum(ye - ys) * STEP;
    for (double cx1 = xs + STEP, cx2 = xe - STEP; cx1 <= cx2; cx1 += STEP) {
      if (tryVertical(line, ys, ye, vs, cx1, cx2)) return false;
    }
    for (double cx1 = xs + STEP, cx2 = xe - STEP; cx2 >= cx1; cx2 -= STEP) {
      if (tryVertical(line, ys, ye, vs, cx1, cx2)) return false;
    }
    return true;
  }

  private static boolean tryVertical(DiagramLine line, double ys, double ye, double vs, double cx1, double cx2) {
    for (int i = 0; i < 10; i++) {
      var dy = i * vs;
      if (line.tryApply(cx1, ys + dy, cx2, ye + dy)) return true;
    }
    for (int i = 1; i < 10; i++) {
      var dy = i * vs;
      if (line.tryApply(cx1, ys - dy, cx2, ye - dy)) return true;
    }
    return false;
  }
}
