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

import javafx.collections.SetChangeListener;
import javafx.collections.WeakSetChangeListener;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tybaco.editors.change.SetChange;
import org.tybaco.editors.model.LibInput;
import org.tybaco.ui.model.Link;

import static java.util.Collections.binarySearch;
import static java.util.Comparator.comparing;

public final class DiagramBlockInput extends BorderPane {

  final DiagramBlock block;
  final LibInput input;
  private final VBox vectorInputs;
  private final Button inputButton;
  private final SetChangeListener<Link> linksListener = this::onLinkChange;
  private final Font defaultFont = Font.font(Font.getDefault().getFamily(), Font.getDefault().getSize());
  private final Font boldFont = Font.font(defaultFont.getFamily(), FontWeight.BOLD, defaultFont.getSize());

  public DiagramBlockInput(DiagramBlock block, LibInput input, String name) {
    this.block = block;
    this.input = input;
    setTop(inputButton = new Button(name));
    inputButton.setFont(defaultFont);
    inputButton.setFocusTraversable(false);
    setCenter(vectorInputs = new VBox());
    initialize();
  }

  private void initialize() {
    var allLinks = block.diagram.project.links;
    allLinks.forEach(l -> linksListener.onChanged(new SetChange<>(allLinks, null, l)));
    allLinks.addListener(new WeakSetChangeListener<>(linksListener));
  }

  private void onLinkChange(SetChangeListener.Change<? extends Link> change) {
    if (change.wasAdded()) onLinkAdded(change.getElementAdded());
    else if (change.wasRemoved()) onLinkRemoved(change.getElementRemoved());
  }

  private void onLinkAdded(Link link) {
    if (!link.inputMatches(block.block, inputButton.getText())) return;
    inputButton.setFont(boldFont);
    if (link.index() >= 0) {
      var b = new Button(Integer.toString(link.index()));
      b.setUserData(link.index());
      var i = binarySearch(vectorInputs.getChildren(), b, comparing(n -> (Integer) n.getUserData()));
      if (i < 0) {
        vectorInputs.getChildren().add(-(i + 1), b);
      }
    }
  }

  private void onLinkRemoved(Link link) {
    if (link.out().blockId() != block.block.id || !link.out().spot().equals(inputButton.getText())) return;
    if (link.index() < 0) {
      inputButton.setFont(defaultFont);
    } else {
      var txtIndex = Integer.toString(link.index());
      vectorInputs.getChildren().removeIf(n -> n instanceof Button b && txtIndex.equals(b.getText()));
      if (vectorInputs.getChildren().isEmpty()) {
        inputButton.setFont(defaultFont);
      }
    }
  }
}
