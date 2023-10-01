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

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tybaco.editors.Meta;
import org.tybaco.editors.control.Tables;
import org.tybaco.editors.model.LibConst;
import org.tybaco.editors.text.Texts;
import org.tybaco.ui.child.project.classpath.Editors;
import org.tybaco.ui.child.project.classpath.ProjectClasspath;

import java.util.List;
import java.util.TreeMap;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static org.tybaco.editors.icon.Icons.icon;
import static org.tybaco.editors.text.Texts.text;

@Scope(SCOPE_PROTOTYPE)
@Component
public final class LibraryConstantsTree extends TreeTableView<Meta> {

  private final ClassLoader classLoader;
  public final BooleanBinding nonConstantSelected;

  public LibraryConstantsTree(Editors editors, ProjectClasspath classpath) {
    super(new TreeItem<>());
    nonConstantSelected = Bindings.createBooleanBinding(() -> {
      var item = getSelectionModel().getSelectedItem();
      return item == null || !(item.getValue() instanceof LibConst);
    }, getSelectionModel().selectedItemProperty());
    classLoader = classpath.classPath.get().classLoader;
    setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    setPadding(Insets.EMPTY);
    setShowRoot(false);
    getColumns().addAll(List.of(nameColumn(), descriptionColumn()));
    Tables.initColumnWidth(this, 300, 500);
    fill(editors);
  }

  private void fill(Editors editors) {
    editors.constLibs.get().stream().sorted().forEach(lib -> {
      var libElem = new TreeItem<Meta>(lib, icon(lib.icon(), 20));
      getRoot().getChildren().add(libElem);
      lib.constants().stream()
        .sorted()
        .collect(groupingBy(c -> Meta.meta(c.getClass().getPackage()), TreeMap::new, toList()))
        .forEach((g, cs) -> {
            var pkgElem = new TreeItem<>(g, icon(g.icon(), 20));
            libElem.getChildren().add(pkgElem);
            cs.forEach(c -> {
              var elem = new TreeItem<Meta>(c, icon(classLoader, c.icon(), 20));
              pkgElem.getChildren().add(elem);
            });
        });
      libElem.setExpanded(true);
    });
  }

  private TreeTableColumn<Meta, String> nameColumn() {
    var col = new TreeTableColumn<Meta, String>();
    col.textProperty().bind(text("Name"));
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
