package org.tybaco.ui.lib.tables;

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

import org.tybaco.ui.lib.event.UniEventListeners;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableModel;

import static javax.swing.event.TableModelEvent.*;

public abstract class TableListModelAdapter<E> implements TableModel, AutoCloseable {

  protected final UniEventListeners<TableModelListener> listeners = new UniEventListeners<>();
  protected final ListListener listListener = new ListListener();
  protected final ListModel<E> listModel;

  protected TableListModelAdapter(ListModel<E> listModel) {
    this.listModel = listModel;
    this.listModel.addListDataListener(listListener);
  }

  @Override
  public final int getRowCount() {
    return listModel.getSize();
  }

  @Override
  public final Object getValueAt(int rowIndex, int columnIndex) {
    return getValueAt(listModel.getElementAt(rowIndex), columnIndex);
  }

  @Override
  public final boolean isCellEditable(int rowIndex, int columnIndex) {
    return isCellEditable(listModel.getElementAt(rowIndex), columnIndex);
  }

  protected abstract Object getValueAt(E element, int columnIndex);
  protected abstract boolean isCellEditable(E element, int columnIndex);

  @Override
  public void addTableModelListener(TableModelListener l) {
    listeners.add(l);
  }

  @Override
  public void removeTableModelListener(TableModelListener l) {
    listeners.remove(l);
  }

  @Override
  public void close() {
    listModel.removeListDataListener(listListener);
  }

  protected final class ListListener implements ListDataListener {

    @Override
    public void intervalAdded(ListDataEvent e) {
      var ev = new TableModelEvent(TableListModelAdapter.this, e.getIndex0(), e.getIndex1(), ALL_COLUMNS, INSERT);
      listeners.forEach(l -> l.tableChanged(ev));
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
      var ev = new TableModelEvent(TableListModelAdapter.this, e.getIndex0(), e.getIndex1(), ALL_COLUMNS, DELETE);
      listeners.forEach(l -> l.tableChanged(ev));
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
      var ev = new TableModelEvent(TableListModelAdapter.this, e.getIndex0(), e.getIndex1(), ALL_COLUMNS, UPDATE);
      listeners.forEach(l -> l.tableChanged(ev));
    }
  }
}
