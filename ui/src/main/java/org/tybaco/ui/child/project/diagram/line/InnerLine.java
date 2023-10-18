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

public final class InnerLine implements Line {

  private final DiagramLine line;

  public InnerLine(DiagramLine line) {
    this.line = line;
  }

  @Override
  public boolean tryApply(double xs, double ys, double xe, double ye) {
    var vs = Math.signum(ye - ys) * STEP;
    for (int i = 7; i < 20; i++) {
      var cx1 = xs + i * STEP;
      var cx2 = xe - i * STEP;
      for (int j = 10; j >= 1; j--) {
        if (line.tryApply(cx1, ys + j * vs, cx2, ye - j * vs)) {
          return true;
        }
      }
    }
    return false;
  }
}
