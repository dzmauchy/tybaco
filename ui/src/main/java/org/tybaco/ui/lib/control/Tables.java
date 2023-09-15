package org.tybaco.ui.lib.control;

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

import javafx.scene.control.TableView;

public interface Tables {

  static void initColumnWidths(TableView<?> table, int... widths) {
    var columns = table.getColumns();
    for (int i = 0; i < widths.length; i++) {
      var column = columns.get(i);
      var width = widths[i];
      column.setMinWidth(width * 0.9);
      column.setPrefWidth(width);
      column.setMaxWidth(width * 5);
    }
  }
}
