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

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.stage.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tybaco.meta.*;
import org.tybaco.ui.child.project.classpath.LibraryFinder;
import org.tybaco.ui.lib.control.Tables;
import org.tybaco.ui.lib.text.Texts;

import java.util.List;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static org.tybaco.logging.Log.info;

@Scope(SCOPE_PROTOTYPE)
@Component
public final class LibraryConstantsTree extends TreeTableView<MetaContainer> {

  public LibraryConstantsTree(LibraryFinder finder) {
    setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    setShowRoot(false);
    setRoot(new TreeItem<>(new Meta("", "", "")));
    getColumns().addAll(List.of(nameColumn(), descriptionColumn()));
    Tables.initColumnWidth(this, 300, 500);
    var thread = new Thread(() -> finder.libraries().forEachOrdered(lib -> {
      info(getClass(), "Loading constants definitions");
      var libElem = new TreeItem<MetaContainer>(lib);
      Platform.runLater(() -> getRoot().getChildren().add(libElem));
      lib.constants().forEach(consts -> {
        var constsElem = new TreeItem<MetaContainer>(consts);
        Platform.runLater(() -> libElem.getChildren().add(constsElem));
        consts.constants().forEach(c -> {
          var cElem = new TreeItem<MetaContainer>(c);
          Platform.runLater(() -> constsElem.getChildren().add(cElem));
        });
      });
      info(getClass(), "Constants definitions loaded");
    }));
    thread.setDaemon(true);
    thread.start();
  }

  private TreeTableColumn<MetaContainer, String> nameColumn() {
    var col = new TreeTableColumn<MetaContainer, String>();
    col.textProperty().bind(Texts.text("Name"));
    col.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getValue().meta().name()));
    return col;
  }

  private TreeTableColumn<MetaContainer, String> descriptionColumn() {
    var col = new TreeTableColumn<MetaContainer, String>();
    col.textProperty().bind(Texts.text("Description"));
    col.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getValue().meta().description()));
    return col;
  }

  @Autowired(required = false)
  public void showDialog(Stage primaryStage) {
    var dialog = new Dialog<LibraryConstant>();
    dialog.initOwner(primaryStage);
    dialog.initModality(Modality.NONE);
    dialog.initStyle(StageStyle.DECORATED);
    dialog.getDialogPane().setContent(this);
    dialog.show();
  }
}
