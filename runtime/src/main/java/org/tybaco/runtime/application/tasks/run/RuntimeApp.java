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

import org.tybaco.runtime.basic.CanBeStarted;

import java.util.LinkedList;

public record RuntimeApp(LinkedList<Ref<CanBeStarted>> tasks, LinkedList<Ref<AutoCloseable>> closeables) implements AutoCloseable {

  public void run() {
    while (true) {
      var ref = tasks.pollFirst();
      if (ref == null) break;
      try {
        ref.ref().start();
      } catch (Throwable e) {
        try {
          close();
        } catch (Throwable x) {
          e.addSuppressed(x);
        }
        throw new IllegalStateException("Unable to run %d".formatted(ref.id()), e);
      }
    }
  }

  @Override
  public void close() {
    var exception = new IllegalStateException("Application close error");
    while (true) {
      var closeable = closeables.pollLast();
      if (closeable == null) break;
      try {
        closeable.ref().close();
      } catch (Throwable e) {
        exception.addSuppressed(new IllegalStateException("Unable to close %d".formatted(closeable.id()), e));
      }
    }
    if (exception.getSuppressed().length > 0) {
      throw exception;
    }
  }
}
