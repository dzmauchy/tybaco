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

import javafx.scene.control.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tybaco.editors.Meta;
import org.tybaco.editors.control.Tables;
import org.tybaco.editors.text.Texts;
import org.tybaco.ui.child.project.classpath.Editors;
import org.tybaco.ui.child.project.classpath.ProjectClasspath;

import java.util.List;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static org.tybaco.editors.icon.Icons.icon;
import static org.tybaco.editors.text.Texts.text;

@Scope(SCOPE_PROTOTYPE)
@Component
public final class LibraryConstantsTree extends TreeTableView<Meta> {

  private final ClassLoader classLoader;

  public LibraryConstantsTree(Editors editors, ProjectClasspath classpath) {
    classLoader = classpath.classPath.get().classLoader;
    setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    setShowRoot(false);
    setRoot(new TreeItem<>(null));
    getColumns().addAll(List.of(nameColumn(), descriptionColumn()));
    Tables.initColumnWidth(this, 300, 500);
    editors.constLibs.get().forEach(lib -> {
      var libElem = new TreeItem<Meta>(lib, icon(lib.icon(), 20));
      getRoot().getChildren().add(libElem);
      lib.constants().forEach(c -> {
        var elem = new TreeItem<Meta>(c, icon(classLoader, c.icon(), 20));
        libElem.getChildren().add(elem);
        libElem.setExpanded(true);
      });
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
