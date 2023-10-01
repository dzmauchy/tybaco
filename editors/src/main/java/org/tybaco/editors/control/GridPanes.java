package org.tybaco.editors.control;

/*-
 * #%L
 * editors
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

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.tybaco.editors.text.Texts;
import org.tybaco.editors.util.SeqMap;

import java.util.concurrent.atomic.AtomicInteger;

public interface GridPanes {

  static GridPane twoColumnPane(Class<?> caller, SeqMap<String, Node> nodes) {
    var pane = new GridPane(5, 5);
    var counter = new AtomicInteger();
    nodes.forEach((k, v) -> {
      var label = new Label();
      label.textProperty().bind(Texts.text(caller.getClassLoader(), k));
      pane.addRow(counter.getAndIncrement(), label, v);
    });
    return pane;
  }
}
