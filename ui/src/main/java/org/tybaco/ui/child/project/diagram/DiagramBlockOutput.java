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

import javafx.scene.control.ToggleButton;
import org.tybaco.editors.model.LibOutput;
import org.tybaco.ui.model.Link;

public final class DiagramBlockOutput extends ToggleButton {

  public final DiagramBlock block;
  public final LibOutput output;
  public final String spot;

  public DiagramBlockOutput(DiagramBlock block, LibOutput output, String spot) {
    super(spot);
    setFocusTraversable(false);
    this.block = block;
    this.output = output;
    this.spot = spot;
  }

  public void onLink(Link link, boolean added) {

  }
}
