package org.tybaco.ui.child.project.blocks;

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
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import org.springframework.stereotype.Component;
import org.tybaco.editors.control.Tables;
import org.tybaco.editors.text.Texts;
import org.tybaco.ui.model.Block;
import org.tybaco.ui.model.Project;

import java.util.List;

@Component
public final class BlocksTable extends TableView<Block> {

  public BlocksTable(Project project) {
    super(project.blocks);
    setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    setEditable(true);
    getColumns().addAll(List.of(nameColumn(), factoryColumn()));
    Tables.initColumnWidths(this, 300, 250);
  }

  private TableColumn<Block, String> nameColumn() {
    var column = new TableColumn<Block, String>();
    column.textProperty().bind(Texts.text("Name"));
    column.setEditable(true);
    column.setCellFactory(TextFieldTableCell.forTableColumn());
    column.setCellValueFactory(c -> c.getValue().name);
    return column;
  }

  private TableColumn<Block, String> factoryColumn() {
    var column = new TableColumn<Block, String>();
    column.textProperty().bind(Texts.text("Factory"));
    column.setEditable(false);
    column.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().factoryId));
    return column;
  }
}
