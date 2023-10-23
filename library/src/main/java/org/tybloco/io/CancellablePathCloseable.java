package org.tybloco.io;

/*-
 * #%L
 * library
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

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;

public final class CancellablePathCloseable implements Closeable {

  private Path path;

  public CancellablePathCloseable(Path path) {
    this.path = path;
  }

  public void cancel() {
    path = null;
  }

  @Override
  public void close() throws IOException {
    if (path != null) {
      Paths.deleteRecursively(path);
    }
  }
}
