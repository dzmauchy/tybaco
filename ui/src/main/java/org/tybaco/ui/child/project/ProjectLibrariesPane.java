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

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import org.springframework.stereotype.Component;
import org.tybaco.ui.lib.control.Tables;
import org.tybaco.ui.model.Lib;
import org.tybaco.ui.model.Project;

import java.util.List;

@Component
public class ProjectLibrariesPane extends BorderPane {

  public ProjectLibrariesPane(ProjectLibrariesTable table) {
    super(table);
  }

  @Component
  public static final class ProjectLibrariesTable extends TableView<Lib> {

    public ProjectLibrariesTable(Project project) {
      setItems(project.libs);
      setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
      setTableMenuButtonVisible(true);
      getColumns().addAll(List.of(groupColumn(), artifactColumn(), versionColumn()));
      Tables.initColumnWidths(this, 100, 150, 100);
    }

    private TableColumn<Lib, String> groupColumn() {
      var col = new TableColumn<Lib, String>("groupId");
      col.setReorderable(false);
      col.setSortable(true);
      return col;
    }

    private TableColumn<Lib, String> artifactColumn() {
      var col = new TableColumn<Lib, String>("artifactId");
      col.setReorderable(false);
      col.setSortable(true);
      return col;
    }

    private TableColumn<Lib, String> versionColumn() {
      var col = new TableColumn<Lib, String>("version");
      col.setReorderable(false);
      col.setSortable(true);
      return col;
    }
  }
}
