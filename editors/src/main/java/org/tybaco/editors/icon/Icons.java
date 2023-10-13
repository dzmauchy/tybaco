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

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.javafx.IkonResolver;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import static org.tybaco.logging.Log.warn;

public final class Icons {

  private static final WeakHashMap<ClassLoader, ConcurrentHashMap<String, Image>> IMAGES = new WeakHashMap<>();
  private static final Font ICON_FONT;

  static {
    var classLoader = Thread.currentThread().getContextClassLoader();
    try (var is = classLoader.getResourceAsStream("META-INF/fonts/KaiseiHarunoUmi-Regular.ttf")) {
      ICON_FONT = Font.loadFont(is, 12d);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static Node icon(String key, int size) {
    return icon(Thread.currentThread().getContextClassLoader(), key, size);
  }

  public static Node icon(ClassLoader classLoader, String key, int size) {
    if (key == null || key.isBlank()) {
      return null;
    }
    if (key.charAt(0) > 255 && key.length() <= 3) {
      var text = new Label(key);
      text.setFont(ICON_FONT);
      text.setStyle("-fx-font-size: " + size + "px");
      text.setTextFill(Color.WHITE);
      return text;
    } else if (key.indexOf('.') > 0) {
      final ConcurrentHashMap<String, Image> map;
      synchronized (IMAGES) {
        map = IMAGES.computeIfAbsent(classLoader, c -> new ConcurrentHashMap<>(64, 0.5f));
      }
      var image = map.computeIfAbsent(key, k -> load(classLoader, k));
      var imageView = new ImageView(image);
      imageView.setFitWidth(size);
      imageView.setFitHeight(size);
      imageView.setSmooth(true);
      return imageView;
    } else {
      var resolver = IkonResolver.getInstance();
      try {
        var handler = resolver.resolve(key);
        var icon = handler.resolve(key);
        return icon == null ? null : icon(icon, size);
      } catch (RuntimeException e) {
        warn(Icons.class, "Unable to resolve {0}", e, key);
        return null;
      }
    }
  }

  public static Node icon(Ikon icon, int size) {
    return FontIcon.of(icon, size, Color.WHITE);
  }

  private static Image load(ClassLoader classLoader, String key) {
    try (var is = classLoader.getResourceAsStream(key)) {
      if (is == null) {
        warn(Icons.class, "Unable to resolve {0}", key);
        return null;
      }
      return new Image(is);
    } catch (Throwable e) {
      warn(Icons.class, "Unable to resolve {0}", e, key);
      return null;
    }
  }
}
