package org.tybloco.editors.control;

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

import javafx.beans.binding.StringBinding;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import org.tybloco.editors.util.SeqMap;

import java.util.concurrent.atomic.AtomicInteger;

import static javafx.beans.binding.Bindings.createStringBinding;
import static javafx.scene.layout.Priority.ALWAYS;
import static javafx.scene.layout.Priority.NEVER;
import static org.tybaco.util.MiscOps.build;

public interface GridPanes {

  static GridPane twoColumnPane(SeqMap<StringBinding, Node> nodes) {
    var pane = new GridPane(5, 5);
    pane.getColumnConstraints().addAll(
      build(new ColumnConstraints(), c -> c.setHgrow(NEVER)),
      build(new ColumnConstraints(), c -> c.setHgrow(ALWAYS))
    );
    var counter = new AtomicInteger();
    nodes.forEach((k, v) -> {
      var label = new Label();
      label.textProperty().bind(createStringBinding(() -> k.get() + ":", k));
      pane.addRow(counter.getAndIncrement(), label, v);
    });
    return pane;
  }
}
