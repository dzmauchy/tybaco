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

public final class RuntimeApp implements AutoCloseable {

  private CloseableRef closeables;

  public void addCloseable(Ref<AutoCloseable> ref) {
    closeables = new CloseableRef(ref, closeables);
  }

  @Override
  public void close() {
    var exception = new IllegalStateException("Application close error");
    for (var c = closeables; c != null; ) {
      var ref = c.ref;
      try {
        ref.ref().close();
      } catch (Throwable e) {
        exception.addSuppressed(new IllegalStateException("Unable to close %d".formatted(ref.id()), e));
      }
      closeables = c = c.prev;
    }
    if (exception.getSuppressed().length > 0) {
      throw exception;
    }
  }

  private record CloseableRef(Ref<AutoCloseable> ref, CloseableRef prev) {}
}
