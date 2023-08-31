package org.tybaco.ui.child.project;

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

import org.jetbrains.annotations.Nls;
import org.springframework.stereotype.Component;
import org.tybaco.ui.lib.tables.TableListModelAdapter;
import org.tybaco.ui.main.projects.Block;
import org.tybaco.ui.main.projects.Project;

@Component
public final class ProjectBlockTableModel extends TableListModelAdapter<Block> {

  public ProjectBlockTableModel(Project project) {
    super(project.blocks);
  }

  @Override
  protected Object getValueAt(Block element, int columnIndex) {
    return switch (columnIndex) {
      case 0 -> element.id;
      case 1 -> element.name.get();
      case 2 -> element.factory + "." + element.selector;
      default -> throw new IndexOutOfBoundsException(columnIndex);
    };
  }

  @Override
  public int getColumnCount() {
    return 3;
  }

  @Nls
  @Override
  public String getColumnName(int columnIndex) {
    return switch (columnIndex) {
      case 0 -> "Id";
      case 1 -> "Name";
      case 2 -> "Type";
      default -> throw new IndexOutOfBoundsException(columnIndex);
    };
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return switch (columnIndex) {
      case 0 -> int.class;
      case 1, 2 -> String.class;
      default -> throw new IndexOutOfBoundsException(columnIndex);
    };
  }
}
