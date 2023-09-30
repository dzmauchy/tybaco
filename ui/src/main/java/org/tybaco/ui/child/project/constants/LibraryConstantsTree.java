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

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tybaco.editors.control.Tables;
import org.tybaco.editors.model.LibConst;
import org.tybaco.ui.child.project.classpath.ProjectClasspath;
import org.tybaco.ui.model.*;
import org.tybaco.editors.Meta;

import java.util.List;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static org.tybaco.editors.icon.Icons.icon;
import static org.tybaco.editors.text.Texts.text;

@Scope(SCOPE_PROTOTYPE)
@Component
public final class LibraryConstantsTree extends TreeTableView<Meta> {

  public LibraryConstantsTree(ProjectClasspath projectClasspath) {
    setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    setShowRoot(false);
    setRoot(new TreeItem<>(null));
    getColumns().addAll(List.of(nameColumn(), descriptionColumn()));
    Tables.initColumnWidth(this, 300, 500);
    projectClasspath.constLibs.get().forEach(lib -> {
      var libElem = new TreeItem<Meta>(lib, icon(lib.icon(), 20));
      getRoot().getChildren().add(libElem);
      lib.constants().forEach(c -> {
        var elem = new TreeItem<Meta>(c, icon(c.icon(), 20));
        libElem.getChildren().add(elem);
        libElem.setExpanded(true);
      });
    });
  }

  private TreeTableColumn<Meta, String> nameColumn() {
    var col = new TreeTableColumn<Meta, String>();
    col.textProperty().bind(text("Name"));
    col.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getValue().name()));
    return col;
  }

  private TreeTableColumn<Meta, String> descriptionColumn() {
    var col = new TreeTableColumn<Meta, String>();
    col.textProperty().bind(text("Description"));
    col.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getValue().description()));
    return col;
  }

  @Scope(SCOPE_PROTOTYPE)
  @Component
  public final class Win extends Dialog<Constant> {

    public Win(@Autowired(required = false) Stage primaryStage, Project project) {
      setWidth(1024);
      setHeight(768);
      initModality(Modality.APPLICATION_MODAL);
      initOwner(primaryStage);
      setResizable(true);
      var textArea = new TextArea();
      var titledPane = new TitledPane(null, textArea);
      titledPane.textProperty().bind(text("Value").map(v -> v + ":"));
      var splitPane = new SplitPane(LibraryConstantsTree.this, titledPane);
      splitPane.setPadding(Insets.EMPTY);
      splitPane.setOrientation(Orientation.VERTICAL);
      splitPane.setDividerPosition(0, 0.7);
      getDialogPane().setContent(splitPane);
      headerTextProperty().bind(text("Select a constant").map(v -> v + ":"));
      titleProperty().bind(text("Constants"));
      getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CLOSE);
      var applyButton = getDialogPane().lookupButton(ButtonType.APPLY);
      applyButton.setDisable(true);
      getSelectionModel().selectedItemProperty().addListener((o, ov, nv) ->
        applyButton.setDisable(nv == null || !(nv.getValue() instanceof LibConst<?>))
      );
      setResultConverter(t -> switch (t.getButtonData()) {
        case APPLY -> {
          var item = getSelectionModel().getSelectedItem();
          if (item != null && item.getValue() instanceof LibConst<?> c) {
            yield project.newConstant("x", "y", textArea.getText());
          } else {
            yield null;
          }
        }
        default -> null;
      });
    }
  }
}
