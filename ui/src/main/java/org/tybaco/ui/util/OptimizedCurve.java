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

import org.jetbrains.annotations.NotNull;

import java.awt.geom.CubicCurve2D;

import static java.lang.Float.compare;

public final class OptimizedCurve extends CubicCurve2D.Float implements Comparable<OptimizedCurve> {

  final float func;

  OptimizedCurve(float x1, float y1, float cx1, float cy1, float cx2, float cy2, float x2, float y2, float func) {
    super(x1, y1, cx1, cy1, cx2, cy2, x2, y2);
    this.func = func;
  }

  @Override
  public int compareTo(@NotNull OptimizedCurve o) {
    return compare(func, o.func);
  }
}
