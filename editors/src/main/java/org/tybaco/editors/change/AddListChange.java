package org.tybaco.editors.change;

/*-
 * #%L
 * editors
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

import javafx.collections.*;
import javafx.collections.ListChangeListener.Change;

import java.util.List;

public final class AddListChange<E> extends Change<E> {

  private final int from;
  private final int to;
  private int state;

  public AddListChange(ObservableList<E> list, int from, int to) {
    super(list);
    this.from = from;
    this.to = to;
  }

  @Override
  public boolean next() {
    return state++ == 0;
  }

  @Override
  public void reset() {
    state = 0;
  }

  @Override
  public int getFrom() {
    if (state == 1) return from;
    throw new IllegalStateException();
  }

  @Override
  public int getTo() {
    if (state == 1) return to;
    throw new IllegalStateException();
  }

  @Override
  public List<E> getRemoved() {
    if (state == 1) return List.of();
    throw new IllegalStateException();
  }

  @Override
  protected int[] getPermutation() {
    if (state == 1) return new int[0];
    throw new IllegalStateException();
  }
}
