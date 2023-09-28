package org.tybaco.editors.action;

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
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.tybaco.editors.icon.IconKey;
import org.tybaco.editors.icon.Icons;
import org.tybaco.editors.text.Texts;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.tybaco.logging.Log.warn;

public abstract class LocalFactory {

  private final ClassLoader classLoader;
  private final ConcurrentHashMap<IconKey, Image> images = new ConcurrentHashMap<>(64, 0.5f);

  public LocalFactory() {
    classLoader = getClass().getClassLoader();
  }

  public StringBinding fmt(String fmt) {
    return Texts.text(classLoader, fmt);
  }

  public StringBinding fmt(String fmt, Object... args) {
    return Texts.text(classLoader, fmt, args);
  }

  public StringBinding msg(String msg) {
    return Texts.msg(classLoader, msg);
  }

  public StringBinding msg(String msg, Object... args) {
    return Texts.msg(classLoader, msg, args);
  }

  public Node icon(String key, int size) {
    if (key == null) {
      return null;
    } else if (key.indexOf('.') > 0) {
      return new ImageView(images.computeIfAbsent(new IconKey(key, size), this::load));
    } else {
      return Icons.icon(key, size);
    }
  }

  private Image load(IconKey key) {
    try (var is = classLoader.getResourceAsStream(key.key())) {
      if (is == null) {
        return null;
      }
      return new Image(is, key.size(), key.size(), false, true);
    } catch (Throwable e) {
      warn(Icons.class, "Unable to resolve {0}", key);
      return null;
    }
  }

  public Node icon(Ikon icon, int size) {
    return FontIcon.of(icon, size, Color.WHITE);
  }

  public final class ActionBuilder {

    private final Action action = new Action();

    public ActionBuilder() {
    }

    public ActionBuilder(String text) {
      action.text(Texts.text(classLoader, text));
    }

    public ActionBuilder(String text, Ikon ikon) {
      this(text);
      action.icon(new SimpleStringProperty(ikon.getDescription()));
    }

    public ActionBuilder(String text, String icon) {
      this(text);
      action.icon(new SimpleStringProperty(icon));
    }

    public ActionBuilder(String text, String icon, EventHandler<ActionEvent> eventHandler) {
      this(text, icon);
      action.handler(eventHandler);
    }

    public ActionBuilder(String text, Ikon ikon, EventHandler<ActionEvent> eventHandler) {
      this(text, ikon);
      action.handler(eventHandler);
    }

    public ActionBuilder(String text, Ikon ikon, String description) {
      this(text, ikon);
      action.description(Texts.text(classLoader, description));
    }

    public ActionBuilder(String text, String icon, String description) {
      this(text, icon);
      action.description(Texts.text(classLoader, description));
    }

    public ActionBuilder(String text, Ikon ikon, String description, EventHandler<ActionEvent> eventHandler) {
      this(text, ikon, description);
      action.handler(eventHandler);
    }

    public ActionBuilder(String text, String icon, String description, EventHandler<ActionEvent> eventHandler) {
      this(text, icon, description);
      action.handler(eventHandler);
    }

    public ActionBuilder selectionBoundTo(Property<Boolean> selection, boolean initial) {
      action.selectionBoundTo(selection, initial);
      return this;
    }

    public ActionBuilder with(Consumer<Action> configurer) {
      configurer.accept(action);
      return this;
    }

    public ActionBuilder group(String group) {
      action.group(group);
      return this;
    }

    public Action build() {
      return action;
    }
  }
}
