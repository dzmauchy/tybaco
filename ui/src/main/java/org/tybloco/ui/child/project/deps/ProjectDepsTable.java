package org.tybloco.ui.child.project.deps;

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

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.springframework.stereotype.Component;
import org.tybloco.editors.control.Tables;
import org.tybloco.ui.model.Dependency;
import org.tybloco.ui.model.Project;

import java.util.List;

@Component
public final class ProjectDepsTable extends TableView<Dependency> {

  public ProjectDepsTable(Project project) {
    setItems(project.dependencies);
    setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    getColumns().addAll(List.of(groupColumn(), artifactColumn(), versionColumn()));
    Tables.initColumnWidths(this, 100, 150, 100);
  }

  private TableColumn<Dependency, String> groupColumn() {
    var col = new TableColumn<Dependency, String>("groupId");
    col.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().group()));
    col.setReorderable(false);
    col.setSortable(true);
    return col;
  }

  private TableColumn<Dependency, String> artifactColumn() {
    var col = new TableColumn<Dependency, String>("artifactId");
    col.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().artifact()));
    col.setReorderable(false);
    col.setSortable(true);
    return col;
  }

  private TableColumn<Dependency, String> versionColumn() {
    var col = new TableColumn<Dependency, String>("version");
    col.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().version()));
    col.setReorderable(false);
    col.setSortable(true);
    return col;
  }
}
