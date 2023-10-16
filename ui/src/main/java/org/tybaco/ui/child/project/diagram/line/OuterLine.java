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
      return tryBottom(xs, ys, xe, ye, ib, ob) || tryTop(xs, ys, xe, ye, ib, ob);
    } else {
      return tryTop(xs, ys, xe, ye, ib, ob) || tryBottom(xs, ys, xe, ye, ib, ob);
    }
  }

  private boolean tryBottom(double xs, double ys, double xe, double ye, Bounds ib, Bounds ob) {
    return false;
  }

  private boolean tryTop(double xs, double ys, double xe, double ye, Bounds ib, Bounds ob) {
    return false;
  }
}
