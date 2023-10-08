package org.tybaco.editors.icon;

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

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.IkonProvider;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.tybaco.editors.text.Texts;

import java.util.*;

import static javafx.application.Platform.runLater;

public class IconViewer extends BorderPane {

  public IconViewer() {
    var icons = FXCollections.observableList(loadIcons());
    var listView = listView(icons);
    setCenter(listView);
    setTop(buttons(listView));
  }

  private ListView<Ikon> listView(ObservableList<Ikon> icons) {
    var listView = new ListView<Ikon>();
    listView.setItems(icons);
    var copyCombinations = List.of(
      new KeyCodeCombination(KeyCode.COPY),
      new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN),
      new KeyCodeCombination(KeyCode.C, KeyCombination.META_DOWN)
    );
    listView.setCellFactory(param -> new TextFieldListCell<>() {
      @Override
      public void updateItem(Ikon item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
          setText((getIndex() + 1) + ": " + item.getDescription());
          setGraphic(FontIcon.of(item, 32, Color.WHITE));
        }
      }
    });
    listView.setOnKeyPressed(e -> {
      if (copyCombinations.stream().anyMatch(c -> c.match(e))) {
        copy(listView);
      }
    });
    return listView;
  }

  private void copy(ListView<Ikon> view) {
    var item = view.getSelectionModel().getSelectedItem();
    if (item != null) {
      var clipboard = Clipboard.getSystemClipboard();
      clipboard.setContent(Map.of(DataFormat.PLAIN_TEXT, item.getDescription()));
    }
  }

  private HBox buttons(ListView<Ikon> view) {
    var forward = new Button(null, FontIcon.of(FontAwesomeSolid.FORWARD, Color.WHITE));
    forward.setFocusTraversable(false);
    var backward = new Button(null, FontIcon.of(FontAwesomeSolid.BACKWARD, Color.WHITE));
    backward.setFocusTraversable(false);
    var copy = new Button(null, FontIcon.of(FontAwesomeSolid.COPY, Color.WHITE));
    copy.setFocusTraversable(false);
    var textField = new TextField();
    forward.setOnAction(e -> {
      e.consume();
      var size = view.getItems().size();
      for (int i = view.getSelectionModel().getSelectedIndex() + 1; i < size; i++) {
        var icon = view.getItems().get(i);
        if (icon.getDescription().contains(textField.getText())) {
          view.getSelectionModel().select(i);
          view.scrollTo(i);
          view.requestFocus();
          return;
        }
      }
      view.getSelectionModel().clearSelection();
    });
    backward.setOnAction(e -> {
      e.consume();
      var from = view.getSelectionModel().getSelectedIndex() < 0 ? view.getItems().size() : view.getSelectionModel().getSelectedIndex();
      for (int i = from - 1; i >= 0; i--) {
        var icon = view.getItems().get(i);
        if (icon.getDescription().contains(textField.getText())) {
          view.getSelectionModel().select(i);
          view.scrollTo(i);
          view.requestFocus();
          return;
        }
      }
      view.getSelectionModel().clearSelection();
    });
    copy.setOnAction(e -> copy(view));
    var box = new HBox(5,
      textField,
      new Separator(Orientation.VERTICAL),
      forward, backward,
      new Separator(Orientation.VERTICAL),
      copy
    );
    box.setPadding(new Insets(5d));
    return box;
  }

  private static List<Ikon> loadIcons() {
    return ServiceLoader.load(IkonProvider.class).stream().parallel()
      .map(ServiceLoader.Provider::get)
      .map(c -> c.getIkon().getEnumConstants())
      .filter(Objects::nonNull)
      .flatMap(Arrays::stream)
      .filter(Ikon.class::isInstance)
      .map(Ikon.class::cast)
      .toList();
  }

  public static void show() {
    var stage = new Stage(StageStyle.DECORATED);
    stage.setScene(new Scene(new IconViewer(), 1024, 768));
    stage.titleProperty().bind(Texts.text("Icons"));
    stage.show();
  }

  public static void main(String... args) {
    Application.launch(App.class, args);
  }

  public static final class App extends Application {

    @Override
    public void init() {
      Application.setUserAgentStylesheet(STYLESHEET_MODENA);
      runLater(() -> com.sun.javafx.css.StyleManager.getInstance().addUserAgentStylesheet("theme/ui.css"));
    }

    @Override
    public void start(Stage primaryStage) {
      primaryStage.setScene(new Scene(new IconViewer(), 1024, 768));
      primaryStage.setTitle("Icons");
      primaryStage.show();
    }
  }
}
