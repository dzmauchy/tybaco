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

import java.util.concurrent.ConcurrentHashMap;

public final class Icons {

  private static final ConcurrentHashMap<IconKey, Image> IMAGES = new ConcurrentHashMap<>(64, 0.5f);
  private static final ConcurrentHashMap<Ikon, String> FKEYS = new ConcurrentHashMap<>(1024, 0.5f);
  private static final ConcurrentHashMap<String, Ikon> RKEYS = new ConcurrentHashMap<>(1024, 0.5f);

  private Icons() {
  }

  public static Node icon(String key, int size) {
    if (key == null) {
      return null;
    }
    if (key.startsWith("IK_")) {
      var icon = RKEYS.get(key);
      if (icon == null) {
        return null;
      }
      var fontIcon = new FontIcon(icon);
      fontIcon.setIconSize(size);
      fontIcon.setIconColor(Color.WHITE);
      return fontIcon;
    } else {
      var image = IMAGES.computeIfAbsent(new IconKey(key, size), k -> new Image(k.key, k.size, k.size, false, true, false));
      return new ImageView(image);
    }
  }

  public static String iconKey(Ikon ikon) {
    return FKEYS.computeIfAbsent(ikon, i -> {
      var k = "IK_" + i.getDescription();
      RKEYS.putIfAbsent(k, ikon);
      return k;
    });
  }

  private record IconKey(String key, int size) {
  }
}
