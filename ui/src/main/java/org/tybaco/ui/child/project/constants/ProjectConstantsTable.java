package org.tybaco.ui.child.project.constants;

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

import jakarta.annotation.PostConstruct;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsFilled;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.tybloco.editors.action.Action;
import org.tybloco.editors.control.Tables;
import org.tybloco.editors.java.Expressions;
import org.tybloco.editors.text.Texts;
import org.tybaco.ui.child.project.classpath.ConstCache;
import org.tybaco.ui.model.Constant;
import org.tybaco.ui.model.Project;

import java.util.List;

import static org.tybloco.editors.java.Expressions.fromText;

@Component
public class ProjectConstantsTable extends TableView<Constant> {

  private final ConstCache constCache;
  private final Action editAction = new Action("Edit", AntDesignIconsFilled.EDIT, "Edit the selected constant");

  public ProjectConstantsTable(Project project, ConstCache constCache) {
    super(project.constants);
    this.constCache = constCache;
    setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    setEditable(true);
    getColumns().addAll(List.of(nameColumn(), factoryColumn(), valueColumn()));
    Tables.initColumnWidths(this, 130, 130, 300);
  }

  @PostConstruct
  private void initialize() {
    editAction
      .installKeyCombination(this, new KeyCodeCombination(KeyCode.ENTER))
      .installDoubleClick(this)
      .handler(e -> {
        var item = getSelectionModel().getSelectedItem();
        if (item == null) return;
        constCache
          .constById(item.factoryId)
          .flatMap(lc -> lc.edit(this, fromText(item.value.get())))
          .ifPresent(expr -> item.value.set(Expressions.toText(expr)));
      })
      .disabled(getSelectionModel().selectedItemProperty().isNull());
  }

  private TableColumn<Constant, String> nameColumn() {
    var col = new TableColumn<Constant, String>();
    col.textProperty().bind(Texts.text("Name"));
    col.setEditable(true);
    col.setCellFactory(TextFieldTableCell.forTableColumn());
    col.setCellValueFactory(c -> c.getValue().name);
    return col;
  }

  private TableColumn<Constant, String> factoryColumn() {
    var col = new TableColumn<Constant, String>();
    col.textProperty().bind(Texts.text("Factory"));
    col.setEditable(false);
    col.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().factoryId));
    return col;
  }

  private TableColumn<Constant, String> valueColumn() {
    var col = new TableColumn<Constant, String>();
    col.textProperty().bind(Texts.text("Value"));
    col.setEditable(false);
    col.setCellValueFactory(c -> Bindings.createStringBinding(() -> {
      var constant = c.getValue();
      var v = constant.value.get();
      return fromText(v).toString();
    }, c.getValue().value));
    return col;
  }

  @Qualifier("constantListAction")
  @Bean
  public Action addConstantAction(Action newConstantAction) {
    return newConstantAction;
  }

  @Qualifier("constantListAction")
  @Bean
  public Action editConstantAction() {
    return editAction;
  }
}
