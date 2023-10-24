package org.tybloco.ui.lib.logging;

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

import javafx.application.Platform;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.*;

import static java.util.prefs.Preferences.userNodeForPackage;
import static javax.swing.event.TableModelEvent.*;

public class UILogHandler extends Handler implements TableModel {

  private final int maxRecords = userNodeForPackage(UILogHandler.class).getInt("max.records", 65536);
  private final ConcurrentLinkedQueue<LogRecord> primordial = new ConcurrentLinkedQueue<>();
  private final ArrayList<LogRecord> records = new ArrayList<>();
  private final ConcurrentLinkedQueue<TableModelListener> listeners = new ConcurrentLinkedQueue<>();

  private volatile boolean flushed;

  @Override
  public void publish(LogRecord record) {
    if (flushed) {
      Platform.runLater(() -> add(record));
    } else {
      primordial.add(record);
    }
  }

  @Override
  public void flush() {
    flushed = true;
  }

  @Override
  public void close() {
  }

  private void add(LogRecord record) {
    Platform.runLater(() -> {
      if (records.size() >= maxRecords) {
        var toDelete = maxRecords - records.size();
        while (records.size() >= maxRecords) records.remove(0);
        var event = new TableModelEvent(this, 0, toDelete, ALL_COLUMNS, DELETE);
        listeners.forEach(l -> l.tableChanged(event));
      }
      var event = new TableModelEvent(this, records.size(), records.size(), ALL_COLUMNS, INSERT);
      records.add(record);
      listeners.forEach(l -> l.tableChanged(event));
    });
  }

  @Override
  public int getRowCount() {
    return records.size();
  }

  @Override
  public int getColumnCount() {
    return 3;
  }

  @Override
  public String getColumnName(int columnIndex) {
    return switch (columnIndex) {
      case 0 -> "Level";
      case 1 -> "Time";
      default -> "Message";
    };
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return switch (columnIndex) {
      case 0 -> Level.class;
      case 1 -> Instant.class;
      default -> String.class;
    };
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    var record = records.get(rowIndex);
    return switch (columnIndex) {
      case 0 -> record.getLevel();
      case 1 -> record.getInstant();
      default -> {
        try {
          var params = record.getParameters();
          if (params == null || params.length == 0) {
            yield record.getMessage();
          } else {
            yield MessageFormat.format(record.getMessage(), params);
          }
        } catch (RuntimeException e) {
          yield record.getMessage();
        }
      }
    };
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
  }

  @Override
  public void addTableModelListener(TableModelListener l) {
    listeners.add(l);
  }

  @Override
  public void removeTableModelListener(TableModelListener l) {
    listeners.remove(l);
  }

  public static UILogHandler getInstance() {
    var logger = LogManager.getLogManager().getLogger("");
    for (var handler : logger.getHandlers()) {
      if (handler instanceof UILogHandler h) {
        return h;
      }
    }
    throw new NoSuchElementException();
  }
}
