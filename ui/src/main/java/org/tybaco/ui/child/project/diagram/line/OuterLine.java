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

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.tybaco.ui.child.project.diagram.DiagramCalculations.boundsIn;

public final class OuterLine implements Line {

  private final DiagramLine line;

  public OuterLine(DiagramLine line) {
    this.line = line;
  }

  @Override
  public boolean tryApply(double xs, double ys, double xe, double ye) {
    var input = line.link.input.get();
    var output = line.link.output.get();
    if (input == null || output == null) return false;
    var ib = boundsIn(line.diagram.blocks, input.block);
    var ob = boundsIn(line.diagram.blocks, output.block);
    if (ib == null || ob == null) return false;
    if (ye > ys) {
      return tryBottom(ys, ib, ob) || tryTop(ys, ib, ob);
    } else {
      return tryTop(ys, ib, ob) || tryBottom(ys, ib, ob);
    }
  }

  private boolean tryBottom(double ys, Bounds ib, Bounds ob) {
    double maxX = max(ib.getMaxX(), ob.getMaxX()), minX = min(ib.getMinX(), ob.getMinX());
    double maxY = max(ib.getMaxY(), ob.getMaxY());
    for (int i = 10; i < 30; i++) {
      double cx1 = maxX + i * STEP, cx2 = minX - i * STEP;
      for (int j = 0; j < 30; j++) {
        if (line.tryApply(cx1, ys + j * STEP, cx2, maxY + j * STEP))
          return true;
      }
    }
    return false;
  }

  private boolean tryTop(double ys, Bounds ib, Bounds ob) {
    double maxX = max(ib.getMaxX(), ob.getMaxX()), minX = min(ib.getMinX(), ob.getMinX());
    double minY = min(ib.getMinY(), ob.getMinY());
    for (int i = 10; i < 30; i++) {
      double cx1 = maxX + i * STEP, cx2 = minX - i * STEP;
      for (int j = 0; j < 30; j++) {
        if (line.tryApply(cx1, ys - j * STEP, cx2, minY - j * STEP))
          return true;
      }
    }
    return false;
  }
}
