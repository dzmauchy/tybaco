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

import org.tybaco.ui.util.ArrayBasedCurveDivider;

final class SimpleLine implements Line {

  private final DiagramLine line;
  private final LineContext context;

  SimpleLine(DiagramLine line, LineContext context) {
    this.line = line;
    this.context = context;
  }

  @Override
  public boolean tryApply() {
    double xs = context.xs(), ys = context.ys(), xe = context.xe(), ye = context.ye();
    if (xe - xs <= 30d) return false;
    double vs = Math.signum(ye - ys) * STEP, w = (xe - xs) / 5d;
    for (double cx1 = xs + w, cx2 = xe - w, r = xe - w; cx1 <= r; cx1 += STEP) {
      if (tryVertical(ys, ye, vs, cx1, cx2)) return true;
    }
    for (double cx1 = xs + w, cx2 = xe - w, l = xs + w; cx2 >= l; cx2 -= STEP) {
      if (tryVertical(ys, ye, vs, cx1, cx2)) return true;
    }
    for (double cx1 = xs + w + STEP, cx2 = xe - w - STEP, l = xs + w, r = xe - w; cx2 >= l && cx1 <= r; cx1 += STEP, cx2 -= STEP) {
      if (tryVertical(ys, ye, vs, cx1, cx2)) return true;
    }
    return false;
  }

  @Override
  public LineContext getContext() {
    return context;
  }

  @Override
  public ArrayBasedCurveDivider getDivider() {
    return D4;
  }

  private boolean tryVertical(double ys, double ye, double vs, double cx1, double cx2) {
    for (int i = 0; i < 6; i++) {
      if (line.tryApply(this, cx1, ys + i * vs, cx2, ye - i * vs)) {
        return true;
      }
      if (line.tryApply(this, cx1, ys - i * vs, cx2, ye - i * vs)) {
        return true;
      }
    }
    return false;
  }
}
