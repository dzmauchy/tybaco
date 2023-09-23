package org.tybaco.runtime.application.tasks.run;

/*-
 * #%L
 * runtime
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

import java.util.Arrays;

public final class Conns {

  private Conn[] conns = new Conn[0];

  public int len() {
    return conns.length;
  }

  public Conn get(int index) {
    return conns[index];
  }

  public void add(int index, Conn conn) {
    if (index < 0) {
      conns = new Conn[] {conn};
    } else {
      if (index >= conns.length) conns = Arrays.copyOf(conns, index + 1, Conn[].class);
      conns[index] = conn;
    }
  }

  public void forEach(Consumer consumer) {
    var conns = this.conns;
    var len = conns.length;
    for (int i = 0; i < len; i++) {
      var conn = conns[i];
      if (conn != null) {
        consumer.consume(i, conn);
      }
    }
  }

  public interface Consumer {
    void consume(int index, Conn conn);
  }
}
