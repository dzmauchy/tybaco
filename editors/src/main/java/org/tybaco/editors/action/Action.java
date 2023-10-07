package org.tybaco.editors.action;

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

import javafx.beans.Observable;
import javafx.beans.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.*;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import org.kordamp.ikonli.Ikon;
import org.tybaco.editors.icon.Icons;
import org.tybaco.editors.text.Texts;

import java.util.*;
import java.util.function.Consumer;

import static javafx.beans.binding.Bindings.createObjectBinding;

public final class Action {

  private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

  private final SimpleObjectProperty<EventHandler<ActionEvent>> handler = new SimpleObjectProperty<>(this, "handler");
  private final SimpleStringProperty text = new SimpleStringProperty(this, "text");
  private final SimpleStringProperty description = new SimpleStringProperty(this, "description");
  private final SimpleObjectProperty<KeyCombination> accelerator = new SimpleObjectProperty<>(this, "accelerator");
  private final SimpleObjectProperty<String> icon = new SimpleObjectProperty<>(this, "icon");
  private final SimpleBooleanProperty selected = new SimpleBooleanProperty(this, "selected");
  private final SimpleBooleanProperty disabled = new SimpleBooleanProperty(this, "disabled");
  private final SimpleListProperty<Action> actions = new SimpleListProperty<>(this, "actions");
  private final SimpleStringProperty group = new SimpleStringProperty(this, "group");

  private final ClassLoader classLoader;
  private String separatorGroup = "";
  private boolean selectionEnabled;
  private int menuIconSize = 20;
  private int buttonIconSize = 24;

  public Action() {
    classLoader = STACK_WALKER.getCallerClass().getClassLoader();
  }

  public Action(String text) {
    this();
    if (text != null) {
      this.text.bind(Texts.text(classLoader, text));
    }
  }

  public Action(String text, String icon, EventHandler<ActionEvent> handler) {
    this(text, icon);
    this.handler.bind(new SimpleObjectProperty<>(handler));
  }

  public Action(String text, Ikon icon, EventHandler<ActionEvent> handler) {
    this(text, icon);
    this.handler.bind(new SimpleObjectProperty<>(handler));
  }

  public Action(String text, String icon) {
    this(text);
    this.icon.bind(new SimpleStringProperty(icon));
  }

  public Action(String text, Ikon icon) {
    this(text);
    this.icon.bind(new SimpleStringProperty(icon.getDescription()));
  }

  public Action(String text, String icon, String description) {
    this(text, icon);
    this.description.bind(Texts.text(classLoader, description));
  }

  public Action(String text, Ikon icon, String description) {
    this(text, icon);
    this.description.bind(Texts.text(classLoader, description));
  }

  public Action(String text, String icon, String description, EventHandler<ActionEvent> handler) {
    this(text, icon, description);
    this.handler.bind(new SimpleObjectProperty<>(handler));
  }

  public Action(String text, Ikon icon, String description, EventHandler<ActionEvent> handler) {
    this(text, icon, description);
    this.handler.bind(new SimpleObjectProperty<>(handler));
  }

  public Action(boolean vertical) {
    this();
    if (vertical) {
      this.description.bind(new SimpleStringProperty("|"));
    }
  }

  public Action fmt(String fmt, Object... args) {
    this.text.bind(Texts.text(classLoader, fmt, args));
    return this;
  }

  public Action msg(String msg, Object... args) {
    this.text.bind(Texts.msg(classLoader, msg, args));
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

  public Action ikon(Ikon ikon) {
    return icon(new SimpleObjectProperty<>(ikon.getDescription()));
  }

  public Action ikon(ObservableValue<Ikon> icon) {
    return icon(createObjectBinding(() -> icon.getValue().getDescription(), icon));
  }

  public Action handler(ObservableValue<EventHandler<ActionEvent>> handler) {
    this.handler.bind(handler);
    return this;
  }

  public Action handler(EventHandler<ActionEvent> handler) {
    this.handler.bind(new SimpleObjectProperty<>(handler));
    return this;
  }

  public Action selectionBoundTo(Property<Boolean> selection, boolean initial) {
    selected.set(initial);
    selection.bind(selected);
    selectionEnabled = true;
    return this;
  }

  public Action disabled(ObservableValue<Boolean> disabled) {
    this.disabled.bind(disabled);
    return this;
  }

  public Action actions(ObservableValue<ObservableList<Action>> actions) {
    this.actions.bind(actions);
    return this;
  }

  public Action actions(ObservableList<Action> actions) {
    return actions(new SimpleObjectProperty<>(actions));
  }

  public Action actions(Collection<Action> actions) {
    return actions(newList(actions));
  }

  public Action actions(Action... actions) {
    return actions(newList(List.of(actions)));
  }

  public Action group(ObservableValue<String> group) {
    this.group.bind(group);
    return this;
  }

  public Action group(String group) {
    return group(new SimpleStringProperty(group));
  }

  public Action separatorGroup(String group) {
    this.separatorGroup = group;
    return this;
  }

  public String getSeparatorGroup() {
    return separatorGroup;
  }

  public boolean isActionsBound() {
    return actions.isBound();
  }

  public boolean isTextBound() {
    return text.isBound();
  }

  public boolean isDescriptionBound() {
    return description.isBound();
  }

  public boolean isAcceleratorBound() {
    return accelerator.isBound();
  }

  public boolean isIconBound() {
    return icon.isBound();
  }

  public boolean isGroupBound() {
    return group.isBound();
  }

  public boolean isSelectionEnabled() {
    return selectionEnabled;
  }

  public boolean isDisabledBound() {
    return disabled.isBound();
  }

  public Action buttonIconSize(int size) {
    buttonIconSize = size;
    return this;
  }

  public Action menuIconSize(int size) {
    menuIconSize = size;
    return this;
  }

  public ObjectBinding<Node> graphic(int size) {
    return Bindings.createObjectBinding(() -> Icons.icon(classLoader, icon.get(), size), icon);
  }

  public ObjectBinding<Tooltip> tooltip() {
    return createObjectBinding(() -> {
      var description = this.description.get();
      return description == null ? null : new Tooltip(description);
    }, description);
  }

  @SafeVarargs
  public final Menu toMenu(Consumer<? super Menu>... consumers) {
    var menu = new Menu();
    menu.disableProperty().bind(disabled);
    menu.textProperty().bind(text);
    menu.graphicProperty().bind(graphic(menuIconSize));
    menu.acceleratorProperty().bind(accelerator);
    menu.onActionProperty().bind(handler);
    menu.setUserData(new ActionUserData());
    for (var consumer : consumers) {
      consumer.accept(menu);
    }
    return menu;
  }

  @SafeVarargs
  public final MenuItem toMenuItem(Consumer<? super MenuItem>... consumers) {
    var menuItem = new MenuItem();
    menuItem.textProperty().bind(text);
    menuItem.disableProperty().bind(disabled);
    menuItem.graphicProperty().bind(graphic(menuIconSize));
    menuItem.acceleratorProperty().bind(accelerator);
    menuItem.onActionProperty().bind(handler);
    menuItem.setUserData(new ActionUserData());
    for (var consumer : consumers) {
      consumer.accept(menuItem);
    }
    return menuItem;
  }

  @SafeVarargs
  public final CheckMenuItem toCheckMenuItem(Consumer<? super CheckMenuItem>... consumers) {
    var menuItem = new CheckMenuItem();
    menuItem.disableProperty().bind(disabled);
    menuItem.graphicProperty().bind(graphic(menuIconSize));
    menuItem.textProperty().bind(text);
    menuItem.onActionProperty().bind(handler);
    menuItem.acceleratorProperty().bind(accelerator);
    menuItem.selectedProperty().bindBidirectional(selected);
    menuItem.setUserData(new ActionUserData());
    for (var consumer : consumers) {
      consumer.accept(menuItem);
    }
    return menuItem;
  }

  @SafeVarargs
  public final RadioMenuItem toRadioMenuItem(Consumer<? super RadioMenuItem>... consumers) {
    var menuItem = new RadioMenuItem();
    menuItem.textProperty().bind(text);
    menuItem.disableProperty().bind(disabled);
    menuItem.graphicProperty().bind(graphic(menuIconSize));
    menuItem.acceleratorProperty().bind(accelerator);
    menuItem.onActionProperty().bind(handler);
    menuItem.selectedProperty().bindBidirectional(selected);
    menuItem.setUserData(new ActionUserData());
    for (var consumer : consumers) {
      consumer.accept(menuItem);
    }
    return menuItem;
  }

  @SafeVarargs
  public final Button toButton(Consumer<? super Button>... consumers) {
    var button = new Button();
    button.disableProperty().bind(disabled);
    button.graphicProperty().bind(graphic(buttonIconSize));
    button.onActionProperty().bind(handler);
    button.tooltipProperty().bind(tooltip());
    button.textProperty().bind(text);
    button.setUserData(new ActionUserData());
    for (var consumer : consumers) {
      consumer.accept(button);
    }
    return button;
  }

  @SafeVarargs
  public final ToggleButton toToggleButton(Consumer<? super ToggleButton>... consumers) {
    var button = new ToggleButton();
    button.selectedProperty().bindBidirectional(selected);
    button.textProperty().bind(text);
    button.disableProperty().bind(disabled);
    button.graphicProperty().bind(graphic(buttonIconSize));
    button.onActionProperty().bind(handler);
    button.tooltipProperty().bind(tooltip());
    button.setUserData(new ActionUserData());
    for (var consumer : consumers) {
      consumer.accept(button);
    }
    return button;
  }

  @SafeVarargs
  public final RadioButton toRadioButton(Consumer<? super RadioButton>... consumers) {
    var button = new RadioButton();
    button.selectedProperty().bindBidirectional(selected);
    button.disableProperty().bind(disabled);
    button.textProperty().bind(text);
    button.graphicProperty().bind(graphic(buttonIconSize));
    button.onActionProperty().bind(handler);
    button.tooltipProperty().bind(tooltip());
    button.setUserData(new ActionUserData());
    for (var consumer : consumers) {
      consumer.accept(button);
    }
    return button;
  }

  @SafeVarargs
  public final MenuButton toMenuButton(Consumer<? super MenuButton>... consumers) {
    var button = new MenuButton();
    button.textProperty().bind(text);
    button.disableProperty().bind(disabled);
    button.graphicProperty().bind(graphic(buttonIconSize));
    button.onActionProperty().bind(handler);
    button.tooltipProperty().bind(tooltip());
    var userData = new ActionUserData();
    button.setUserData(userData);
    userData.invalidationListener = o -> button.getItems().setAll(groupItems());
    actions.addListener(new WeakInvalidationListener(userData.invalidationListener));
    userData.invalidationListener.invalidated(actions);
    for (var consumer : consumers) {
      consumer.accept(button);
    }
    return button;
  }

  @SafeVarargs
  public final ContextMenu toContextMenu(Consumer<? super ContextMenu>... consumers) {
    var contextMenu = new ContextMenu();
    contextMenu.getItems().addAll(groupItems());
    for (var consumer : consumers) {
      consumer.accept(contextMenu);
    }
    return contextMenu;
  }

  @SafeVarargs
  public final Tab toTab(Consumer<? super Tab>... consumers) {
    var tab = new Tab();
    tab.textProperty().bind(text);
    tab.disableProperty().bind(disabled);
    tab.graphicProperty().bind(graphic(menuIconSize));
    tab.tooltipProperty().bind(tooltip());
    tab.setUserData(new ActionUserData());
    for (var consumer : consumers) {
      consumer.accept(tab);
    }
    return tab;
  }

  @SafeVarargs
  public final Menu toSmartMenu(Consumer<? super MenuItem>... consumers) {
    var menu = toSmartMenuItem(consumers);
    return menu instanceof Menu m ? m : toMenu(consumers);
  }

  @SafeVarargs
  public final MenuItem toSmartMenuItem(Consumer<? super MenuItem>... consumers) {
    if (selectionEnabled) {
      if (group.isBound()) {
        return toRadioMenuItem(consumers);
      } else {
        return toCheckMenuItem(consumers);
      }
    } else {
      if (actions.isBound()) {
        var menu = toMenu(consumers);
        var data = (ActionUserData) menu.getUserData();
        data.invalidationListener = o -> menu.getItems().setAll(groupItems(consumers));
        actions.addListener(new WeakInvalidationListener(data.invalidationListener));
        data.invalidationListener.invalidated(actions);
        return menu;
      } else if (text.isBound() || icon.isBound()) {
        return toMenuItem(consumers);
      } else {
        return new SeparatorMenuItem();
      }
    }
  }

  @SafeVarargs
  private List<MenuItem> groupItems(Consumer<? super MenuItem>... consumers) {
    var map = new TreeMap<String, LinkedList<MenuItem>>();
    var groups = new TreeMap<String, ToggleGroup>();
    var count = 0;
    for (var action : actions) {
      var menuItem = action.toSmartMenuItem(consumers);
      if (menuItem instanceof RadioMenuItem i) {
        i.setToggleGroup(groups.computeIfAbsent(action.group.get(), g -> new ToggleGroup()));
      }
      map.computeIfAbsent(action.separatorGroup, v -> new LinkedList<>()).addLast(menuItem);
      count++;
    }
    var newItems = new ArrayList<MenuItem>(count + map.size());
    var lastSeparatorGroup = map.pollFirstEntry();
    if (lastSeparatorGroup != null) {
      newItems.addAll(lastSeparatorGroup.getValue());
      map.forEach((k, v) -> {
        newItems.add(new SeparatorMenuItem());
        newItems.addAll(v);
      });
      return newItems;
    } else {
      return List.of();
    }
  }

  @SuppressWarnings("unchecked")
  @SafeVarargs
  public final Control toSmartButton(Map<String, ToggleGroup> map, Consumer<? super EventTarget>... consumers) {
    if (selectionEnabled) {
      if (group.isBound()) {
        var button = toRadioButton(consumers);
        button.toggleGroupProperty().bind(
          createObjectBinding(() -> map.computeIfAbsent(group.get(), k -> new ToggleGroup()), group)
        );
        return button;
      } else {
        return toToggleButton(consumers);
      }
    } else {
      if (actions.isBound()) {
        var button = toMenuButton(consumers);
        var data = (ActionUserData) button.getUserData();
        data.invalidationListener = o -> {
          var m = new TreeMap<String, LinkedList<MenuItem>>();
          var groups = new TreeMap<String, ToggleGroup>();
          var newItems = new ArrayList<MenuItem>();
          for (var action : (Collection<Action>) o) {
            var menuItem = action.toSmartMenuItem(consumers);
            m.computeIfAbsent(action.separatorGroup, k -> new LinkedList<>()).addLast(menuItem);
            if (menuItem instanceof RadioMenuItem i) {
              var group = groups.computeIfAbsent(action.group.get(), g -> new ToggleGroup());
              i.setToggleGroup(group);
            }
          }
          var firstSeparatorGroup = m.pollFirstEntry();
          if (firstSeparatorGroup != null) {
            newItems.addAll(firstSeparatorGroup.getValue());
            m.forEach((k, v) -> {
              newItems.add(new SeparatorMenuItem());
              newItems.addAll(v);
            });
          }
          button.getItems().setAll(newItems);
        };
        actions.addListener(new WeakInvalidationListener(data.invalidationListener));
        data.invalidationListener.invalidated(actions);
        return button;
      } else if (text.isBound() || icon.isBound()) {
        return toButton(consumers);
      } else {
        var separator = new Separator();
        separator.orientationProperty().bind(createObjectBinding(() -> {
          var d = description.get();
          return "|".equals(d) ? Orientation.VERTICAL : Orientation.HORIZONTAL;
        }, description));
        return separator;
      }
    }
  }

  public ObservableList<Action> newList(Collection<Action> actions) {
    return FXCollections.observableList(new ArrayList<>(actions), a -> new Observable[]{
      this.handler,
      this.text,
      this.description,
      this.accelerator,
      this.icon,
      this.selected,
      this.group
    });
  }

  private static final class ActionUserData {
    private InvalidationListener invalidationListener;
  }
}
