package org.tybaco.ui.child.logging;

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

import org.tybaco.ui.lib.context.UIComponent;
import org.tybaco.ui.lib.logging.UILogHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.tybaco.ui.lib.tables.Tables.initColumns;

@UIComponent
public class LogTable extends JTable {

  public LogTable() {
    super(UILogHandler.getInstance());
    setShowGrid(true);
    setAutoResizeMode(AUTO_RESIZE_LAST_COLUMN);
    initColumns(this, 80, 300, 500);
    setDefaultRenderer(Instant.class, new InstantCellRenderer());
  }

  private static final class InstantCellRenderer extends DefaultTableCellRenderer {

    private Object value(Object value) {
      if (value instanceof Instant v) {
        return v.truncatedTo(ChronoUnit.MILLIS).toString();
      } else {
        return null;
      }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      return super.getTableCellRendererComponent(table, value(value), isSelected, hasFocus, row, column);
    }
  }
}
