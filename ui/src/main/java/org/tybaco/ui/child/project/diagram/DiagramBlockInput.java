package org.tybaco.ui.child.project.diagram;

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

import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.tybaco.editors.model.LibInput;

public final class DiagramBlockInput extends BorderPane {

  final DiagramBlock block;
  final LibInput input;
  private final VBox vectorInputs;
  private final Button inputButton;

  public DiagramBlockInput(DiagramBlock block, LibInput input, String name) {
    this.block = block;
    this.input = input;
    setTop(inputButton = new Button(name));
    inputButton.setFocusTraversable(false);
    setCenter(vectorInputs = new VBox());
  }
}
