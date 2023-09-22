package org.tybaco.runtime.basic.source;

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

public final class SequentialSink<E> {

  public final Thread thread;
  public final Source<E> source;

  public SequentialSink(ThreadGroup threadGroup, String name, Source<E> source) {
    this.thread = new Thread(threadGroup, this::run, name);
    this.source = source;
  }

  public void daemon(boolean daemon) {
    thread.setDaemon(daemon);
  }

  public boolean daemon() {
    return thread.isDaemon();
  }

  public boolean alive() {
    return thread.isAlive();
  }

  private void run() {

  }
}
