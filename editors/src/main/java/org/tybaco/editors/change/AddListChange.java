package org.tybaco.editors.change;

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
