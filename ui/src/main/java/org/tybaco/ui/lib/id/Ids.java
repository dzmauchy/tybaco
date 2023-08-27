package org.tybaco.ui.lib.id;

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

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;

public final class Ids {

  private Ids() {
  }

  public static String newId() {
    var time = System.currentTimeMillis();
    var tlr = ThreadLocalRandom.current();
    var raw = ByteBuffer.allocate(16)
      .putLong(0, (time << 16) | (tlr.nextLong() & 0xffffL))
      .putLong(8, tlr.nextLong())
      .array();
    return new BigInteger(1, raw).toString(Character.MAX_RADIX);
  }
}
