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
import org.kordamp.ikonli.IkonProvider;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.logging.Level.INFO;
import static org.tybaco.ui.lib.logging.Logging.LOG;

public final class Icons {

  private static final ConcurrentHashMap<IconKey, Image> IMAGES = new ConcurrentHashMap<>(64, 0.5f);
  public static final Map<String, Ikon> IKONS;

  static {
    LOG.log(INFO, "Loading icons");
    var map = new ConcurrentHashMap<String, Ikon>(1024, 0.5f);
    var loader = ServiceLoader.load(IkonProvider.class);
    try {
      loader.stream().parallel()
        .map(ServiceLoader.Provider::get)
        .map(IkonProvider::getIkon)
        .filter(Class::isEnum)
        .forEach(c -> {
          var values = c.getEnumConstants();
          for (var v : values) {
            if (v instanceof Ikon icon) {
              map.put(icon.getDescription(), icon);
            }
          }
        });
    } finally {
      loader.reload();
    }
    IKONS = Map.copyOf(map);
    LOG.log(INFO, "{0} icons loaded", IKONS.size());
  }

  private Icons() {
  }

  public static Node icon(String key, int size) {
    if (key == null) {
      return null;
    } else {
      var ikon = IKONS.get(key);
      if (ikon == null) {
        var image = IMAGES.computeIfAbsent(
          new IconKey(key, size),
          k -> new Image(k.key, k.size, k.size, false, true, false)
        );
        return new ImageView(image);
      } else {
        return icon(ikon, size);
      }
    }
  }

  public static Node icon(Ikon icon, int size) {
    var fontIcon = new FontIcon(icon);
    fontIcon.setIconSize(size);
    fontIcon.setIconColor(Color.WHITE);
    return fontIcon;
  }

  private record IconKey(String key, int size) {
  }
}
