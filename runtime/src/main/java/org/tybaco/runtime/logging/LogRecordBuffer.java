package org.tybaco.runtime.logging;

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

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

final class LogRecordBuffer extends AbstractCollection<LogRecord> {

  private final LogRecord[] buf;
  private int size;

  public LogRecordBuffer(int size) {
    this.buf = new LogRecord[size];
  }

  @Override
  public boolean add(LogRecord record) {
    buf[size++] = record;
    return true;
  }

  @NotNull
  @Override
  public Iterator<LogRecord> iterator() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public void forEach(Consumer<? super LogRecord> action) {
    int len = size;
    for (int i = 0; i < len; i++) action.accept(buf[i]);
  }

  void reset() {
    Arrays.fill(buf, 0, size, null);
    size = 0;
  }

  int maxSize() {
    return buf.length;
  }
}
