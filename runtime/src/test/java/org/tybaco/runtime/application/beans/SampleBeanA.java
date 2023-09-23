package org.tybaco.runtime.application.beans;

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

public class SampleBeanA implements CanBeStarted, AutoCloseable {

  public static boolean started;
  public static boolean closed;

  private SampleBeanA() {
    started = false;
    closed = false;
  }

  @Override
  public void close() {
    closed = true;
  }

  @Override
  public void start() {
    started = true;
  }

  public static SampleBeanA sampleBeanA() {
    return new SampleBeanA();
  }
}
