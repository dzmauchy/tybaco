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

import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.IkonProvider;
import org.kordamp.ikonli.javafx.FontIcon;
import org.tybaco.editors.text.Texts;

import java.util.ArrayList;
import java.util.ServiceLoader;

public class IconViewer extends ListView<Ikon> {

  public IconViewer() {
    var list = new ArrayList<Ikon>();
    var loader = ServiceLoader.load(IkonProvider.class);
    for (var provider : loader) {
      var values = provider.getIkon().getEnumConstants();
      if (values != null) {
        for (var v : values) {
          if (v instanceof Ikon ikon) {
            list.add(ikon);
          }
        }
      }
    }
    var icons = FXCollections.observableList(list);
    setItems(FXCollections.observableList(icons));
    setCellFactory(param -> new TextFieldListCell<>() {
      @Override
      public void updateItem(Ikon item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
          setText(item.getDescription());
          var icon = new FontIcon(item);
          icon.setIconSize(32);
          icon.setIconColor(Color.WHITE);
          setGraphic(icon);
        }
      }
    });
  }

  public static void show() {
    var stage = new Stage(StageStyle.DECORATED);
    stage.setScene(new Scene(new IconViewer(), 1024, 768));
    stage.titleProperty().bind(Texts.text("Icons"));
    stage.show();
  }
}
