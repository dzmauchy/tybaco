package org.tybaco.ui.lib.action;

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

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import org.tybaco.ui.lib.icon.Icons;
import org.tybaco.ui.lib.text.Texts;

import java.util.function.Consumer;

import static javafx.beans.binding.Bindings.createObjectBinding;

@SuppressWarnings("DuplicatedCode")
public final class Action {

  private final SimpleObjectProperty<EventHandler<ActionEvent>> handler = new SimpleObjectProperty<>(this, "handler");
  private final SimpleStringProperty text = new SimpleStringProperty(this, "text");
  private final SimpleStringProperty description = new SimpleStringProperty(this, "description");
  private final SimpleObjectProperty<KeyCombination> accelerator = new SimpleObjectProperty<>(this, "accelerator");
  private final SimpleObjectProperty<String> icon = new SimpleObjectProperty<>(this, "icon");
  private final SimpleBooleanProperty selected = new SimpleBooleanProperty(this, "selected");

  public Action() {
  }

  public Action(String text) {
    this.text.bind(Texts.text(text));
  }

  public Action(String text, String icon, EventHandler<ActionEvent> handler) {
    this(text, icon);
    this.handler.bind(new SimpleObjectProperty<>(handler));
  }

  public Action(String text, String icon) {
    this(text);
    this.icon.bind(new SimpleStringProperty(icon));
  }

  public Action(String text, String icon, String description) {
    this(text, icon);
    this.description.bind(Texts.text(description));
  }

  public Action(String text, String icon, String description, EventHandler<ActionEvent> handler) {
    this(text, icon, description);
    this.handler.bind(new SimpleObjectProperty<>(handler));
  }

  public Action fmt(String fmt, Object... args) {
    this.text.bind(Texts.text(fmt, args));
    return this;
  }

  public Action msg(String msg, Object... args) {
    this.text.bind(Texts.msg(msg, args));
    return this;
  }

  public Action accelerator(KeyCombination combination) {
    this.accelerator.bind(new SimpleObjectProperty<>(combination));
    return this;
  }

  public Action accelerator(ObservableValue<KeyCombination> combination) {
    this.accelerator.bind(combination);
    return this;
  }

  public Action text(ObservableValue<String> text) {
    this.text.bind(text);
    return this;
  }

  public Action description(ObservableValue<String> text) {
    this.description.bind(text);
    return this;
  }

  public Action icon(ObservableValue<String> icon) {
    this.icon.bind(icon);
    return this;
  }

  public Action handler(ObservableValue<EventHandler<ActionEvent>> handler) {
    this.handler.bind(handler);
    return this;
  }

  public Action selected(ObservableBooleanValue selected) {
    this.selected.bind(selected);
    return this;
  }

  public ObjectBinding<Node> graphic(int size) {
    return createObjectBinding(() -> Icons.icon(icon.get(), size), icon);
  }

  public ObjectBinding<Tooltip> tooltip() {
    return createObjectBinding(() -> {
      var description = this.description.get();
      return description == null ? null : new Tooltip(description);
    }, description);
  }

  @SafeVarargs
  public final Menu toMenu(Consumer<Menu>... consumers) {
    var menu = new Menu();
    menu.textProperty().bind(text);
    menu.graphicProperty().bind(graphic(20));
    menu.acceleratorProperty().bind(accelerator);
    menu.onActionProperty().bind(handler);
    for (var consumer : consumers) {
      consumer.accept(menu);
    }
    return menu;
  }

  @SafeVarargs
  public final MenuItem toMenuItem(Consumer<MenuItem>... consumers) {
    var menuItem = new MenuItem();
    menuItem.textProperty().bind(text);
    menuItem.graphicProperty().bind(graphic(20));
    menuItem.acceleratorProperty().bind(accelerator);
    menuItem.onActionProperty().bind(handler);
    for (var consumer : consumers) {
      consumer.accept(menuItem);
    }
    return menuItem;
  }

  @SafeVarargs
  public final Button toButton(Consumer<Button>... consumers) {
    var button = new Button();
    button.textProperty().bind(text);
    button.graphicProperty().bind(graphic(24));
    button.onActionProperty().bind(handler);
    button.tooltipProperty().bind(tooltip());
    for (var consumer : consumers) {
      consumer.accept(button);
    }
    return button;
  }

  @SafeVarargs
  public final ToggleButton toToggleButton(Consumer<ToggleButton>... consumers) {
    var button = new ToggleButton();
    button.selectedProperty().bind(selected);
    button.textProperty().bind(text);
    button.graphicProperty().bind(graphic(24));
    button.onActionProperty().bind(handler);
    button.tooltipProperty().bind(tooltip());
    for (var consumer : consumers) {
      consumer.accept(button);
    }
    return button;
  }

  @SafeVarargs
  public final RadioButton toRadioButton(Consumer<RadioButton>... consumers) {
    var button = new RadioButton();
    button.selectedProperty().bind(selected);
    button.textProperty().bind(text);
    button.graphicProperty().bind(graphic(24));
    button.onActionProperty().bind(handler);
    button.tooltipProperty().bind(tooltip());
    for (var consumer : consumers) {
      consumer.accept(button);
    }
    return button;
  }

  @SafeVarargs
  public final Tab toTab(Consumer<Tab>... consumers) {
    var tab = new Tab();
    tab.textProperty().bind(text);
    tab.graphicProperty().bind(graphic(20));
    tab.tooltipProperty().bind(tooltip());
    for (var consumer : consumers) {
      consumer.accept(tab);
    }
    return tab;
  }
}
