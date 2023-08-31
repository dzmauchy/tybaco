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

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

public final class Ids {

  private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

  private Ids() {
  }

  public static String newId() {
    var rnd = ThreadLocalRandom.current().nextLong();
    var raw = ByteBuffer.allocate(12)
      .putLong(0, (System.currentTimeMillis() << 8) | (rnd & 0xffL))
      .putInt(8, (int) ((rnd >>> 8) & 0xffff_ffffL))
      .array();
    return ENCODER.encodeToString(raw);
  }
}
