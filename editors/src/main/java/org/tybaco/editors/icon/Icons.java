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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.javafx.IkonResolver;

import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import static org.tybaco.logging.Log.warn;

public final class Icons {

  private static final WeakHashMap<ClassLoader, ConcurrentHashMap<IconKey, Image>> IMAGES = new WeakHashMap<>();

  public static Node icon(String key, int size) {
    return icon(Thread.currentThread().getContextClassLoader(), key, size);
  }

  public static Node icon(ClassLoader classLoader, String key, int size) {
    if (key == null) {
      return null;
    }
    final ConcurrentHashMap<IconKey, Image> map;
    synchronized (IMAGES) {
      map = IMAGES.computeIfAbsent(classLoader, c -> new ConcurrentHashMap<>(64, 0.5f));
    }
    if (key.indexOf('.') > 0) {
      return new ImageView(map.computeIfAbsent(new IconKey(key, size), k -> load(classLoader, k)));
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

  private static Image load(ClassLoader classLoader, IconKey key) {
    try (var is = classLoader.getResourceAsStream(key.key())) {
      if (is == null) {
        warn(Icons.class, "Unable to resolve {0}", key);
        return null;
      }
      return new Image(is, key.size(), key.size(), false, true);
    } catch (Throwable e) {
      warn(Icons.class, "Unable to resolve {0}", e, key);
      return null;
    }
  }
}
