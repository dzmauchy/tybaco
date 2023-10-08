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
import org.tybaco.editors.Meta;
import org.tybaco.editors.control.Tables;
import org.tybaco.editors.model.MetaLib;
import org.tybaco.editors.text.TextSupport;
import org.tybaco.editors.text.Texts;
import org.tybaco.ui.child.project.classpath.ProjectClasspath;

import java.util.List;
import java.util.TreeMap;
import java.util.function.Predicate;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.tybaco.editors.icon.Icons.icon;

public abstract class MetaTree<L extends MetaLib> extends TreeTableView<Meta> implements TextSupport {

  private final SimpleObjectProperty<List<L>> libs;
  private final ClassLoader classLoader;
  public final BooleanBinding nonLeafSelected;

  protected MetaTree(SimpleObjectProperty<List<L>> libs, ProjectClasspath classpath, Predicate<? super Meta> isLeaf) {
    super(new TreeItem<>());
    this.nonLeafSelected = Bindings.createBooleanBinding(
      () -> {
        var item = getSelectionModel().getSelectedItem();
        return item == null || !isLeaf.test(item.getValue());
      },
      getSelectionModel().selectedItemProperty()
    );
    this.classLoader = classpath.getClassLoader();
    this.libs = libs;
    setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    setPadding(Insets.EMPTY);
    setShowRoot(false);
    getColumns().addAll(List.of(nameColumn(), descriptionColumn()));
    Tables.initColumnWidths(this, 300, 500);
    fill();
  }

  private void fill() {
    libs.get().stream().sorted().forEach(lib -> {
      var libElem = new TreeItem<Meta>(lib);
      getRoot().getChildren().add(libElem);
      lib.children().stream()
        .sorted()
        .collect(groupingBy(c -> Meta.meta(c.getClass().getPackage()), TreeMap::new, toList()))
        .forEach((g, cs) -> {
          var pkgElem = new TreeItem<>(g);
          libElem.getChildren().add(pkgElem);
          cs.forEach(c -> {
            var elem = new TreeItem<Meta>(c);
            pkgElem.getChildren().add(elem);
          });
        });
      libElem.setExpanded(true);
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
