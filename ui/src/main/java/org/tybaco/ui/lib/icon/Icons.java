package org.tybaco.ui.lib.icon;

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

import java.util.concurrent.ConcurrentHashMap;

import static java.util.logging.Level.WARNING;
import static org.tybaco.ui.lib.logging.Logging.LOG;

public final class Icons {

  private static final ConcurrentHashMap<IconKey, Image> IMAGES = new ConcurrentHashMap<>(64, 0.5f);

  public static Node icon(String key, int size) {
    if (key == null) {
      return null;
    } else if (key.indexOf('.') > 0) {
      var image = IMAGES.computeIfAbsent(
        new IconKey(key, size),
        k -> new Image(k.key, k.size, k.size, false, true, false)
      );
      return new ImageView(image);
    } else {
      var resolver = IkonResolver.getInstance();
      try {
        var handler = resolver.resolve(key);
        var icon = handler.resolve(key);
        return icon == null ? null : icon(icon, size);
      } catch (RuntimeException ignore) {
        LOG.log(WARNING, "Unable to resolve {0}", key);
        return null;
      }
    }
  }

  public static Node icon(Ikon icon, int size) {
    return FontIcon.of(icon, size, Color.WHITE);
  }

  private record IconKey(String key, int size) {
  }
}
