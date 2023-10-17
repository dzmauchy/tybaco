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

import static java.lang.ThreadLocal.withInitial;
import static java.util.stream.IntStream.range;

public final class CurveDividers {

  private static final ThreadLocal<CurveDivider[]> CURVE_DIVIDERS = withInitial(() -> range(0, 6)
    .mapToObj(CurveDivider::new)
    .toArray(CurveDivider[]::new)
  );

  public static CurveDivider curveDivider(int steps) {
    return CURVE_DIVIDERS.get()[steps];
  }
}
