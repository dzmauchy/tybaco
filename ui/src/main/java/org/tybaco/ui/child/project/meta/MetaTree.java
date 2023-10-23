package org.tybaco.ui.child.project.meta;

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

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import org.tybloco.editors.Meta;
import org.tybloco.editors.control.Tables;
import org.tybloco.editors.model.MetaLib;
import org.tybloco.editors.text.TextSupport;
import org.tybloco.editors.text.Texts;
import org.tybaco.ui.child.project.classpath.ProjectClasspath;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.tybloco.editors.icon.Icons.icon;

public abstract class MetaTree<L extends MetaLib> extends TreeTableView<Meta> implements TextSupport {

  private final ClassLoader classLoader;
  public final BooleanBinding nonLeafSelected;

  protected MetaTree(SimpleObjectProperty<List<? extends L>> libs, ProjectClasspath classpath, Predicate<? super Meta> isLeaf) {
    super(new TreeItem<>());
    this.nonLeafSelected = Bindings.createBooleanBinding(
      () -> {
        var item = getSelectionModel().getSelectedItem();
        return item == null || !isLeaf.test(item.getValue());
      },
      getSelectionModel().selectedItemProperty()
    );
    this.classLoader = classpath.getClassLoader();
    setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    setPadding(Insets.EMPTY);
    setShowRoot(false);
    getColumns().addAll(List.of(nameColumn(), descriptionColumn()));
    Tables.initColumnWidths(this, 300, 500);
    fill(getRoot(), libs.get()::stream);
  }

  private void fill(TreeItem<Meta> item, Supplier<Stream<? extends MetaLib>> libs) {
    libs.get().forEach(lib -> {
      var libItem = new TreeItem<Meta>(lib);
      item.getChildren().add(libItem);
      fill(libItem, lib::childLibs);
      lib.children().forEach(elem -> {
        var elemItem = new TreeItem<Meta>(elem);
        libItem.getChildren().add(elemItem);
      });
    });
  }

  public Meta getSelectedValue() {
    var item = getSelectionModel().getSelectedItem();
    return item == null ? null : item.getValue();
  }

  private TreeTableColumn<Meta, String> nameColumn() {
    var col = new TreeTableColumn<Meta, String>();
    col.textProperty().bind(text("Name"));
    col.setCellFactory(c -> new TreeTableCell<>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
          setText(null);
          setGraphic(null);
        } else {
          var e = getTableRow().getTreeItem().getValue();
          setText(getItem());
          setGraphic(icon(classLoader, e.icon(), 20));
        }
      }
    });
    col.setCellValueFactory(f -> Texts.text(classLoader, f.getValue().getValue().name()));
    return col;
  }

  private TreeTableColumn<Meta, String> descriptionColumn() {
    var col = new TreeTableColumn<Meta, String>();
    col.textProperty().bind(text("Description"));
    col.setCellValueFactory(f -> Texts.text(classLoader, f.getValue().getValue().description()));
    return col;
  }
}
