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

import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;

import static java.util.stream.DoubleStream.iterate;

public final class AdaptiveLine {

  private static final double STEP = 30d;

  private AdaptiveLine() {
  }

  static boolean tryApply(DiagramLine line, double xs, double ys, double xe, double ye) {
    var blockObserver = line.diagram.diagramBlockBoundsObserver;
    var output = line.link.output.get();
    var input = line.link.input.get();
    if (output == null || input == null) return false;
    var ob = blockObserver.get(output.block);
    var ib = blockObserver.get(input.block);
    if (ob == null || ib == null) return false;
    return apply(line,
      xs + STEP,
      (xs + xe) * 0.5,
      ys,
      ys - ob.getMinY() < ob.getMaxY() - ys ? ob.getMinY() - 10d * STEP : ob.getMaxY() + 10d * STEP,
      xe - STEP,
      (xs + xe) * 0.5,
      ye,
      ye - ib.getMinY() < ib.getMaxY() - ye ? ib.getMinY() - 10d * STEP : ib.getMaxY() + 10d * STEP
    );
  }

  private static boolean apply(
    DiagramLine line,
    double cx1s, double cx1e, double cy1s, double cy1e,
    double cx2s, double cx2e, double cy2s, double cy2e
  ) {
    return
      iterate(cx1s, p(cx1s, cx1e), n(cx1s, cx1e)).anyMatch(cx1 ->
        iterate(cy1s, p(cy1s, cy1e), n(cy1s, cy1e)).anyMatch(cy1 ->
          iterate(cx2s, p(cx2s, cx2e), n(cx2s, cx2e)).anyMatch(cx2 ->
            iterate(cy2s, p(cy2s, cy2e), n(cy2s, cy2e)).anyMatch(cy2 ->
              line.tryApply(cx1, cy1, cx2, cy2)
            )
          )
        )
      );
  }

  private static DoublePredicate p(double start, double end) {
    return start < end ? e -> e < end : e -> e > end;
  }

  private static DoubleUnaryOperator n(double start, double end) {
    return start < end ? e -> e + STEP : e -> e - STEP;
  }
}
