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

import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.logging.Level.INFO;
import static org.tybaco.ui.lib.logging.Logging.LOG;

public final class Icons {

  private static final ConcurrentHashMap<IconKey, Image> IMAGES = new ConcurrentHashMap<>(64, 0.5f);
  static final ConcurrentHashMap<String, Ikon> ICONS = new ConcurrentHashMap<>(32768, 0.5f);
  private static final CountDownLatch LATCH = new CountDownLatch(1);

  static {
    LOG.log(INFO, "Loading icons");
    var thread = new Thread(() -> {
      try {
        var loader = ServiceLoader.load(IkonProvider.class);
        for (var provider : loader) {
          var ik = provider.getIkon();
          var values = ik.getEnumConstants();
          if (values != null) {
            for (var v : values) {
              if (v instanceof Ikon icon) {
                ICONS.put(icon.getDescription(), icon);
              }
            }
          }
        }
      } finally {
        LATCH.countDown();
      }
      LOG.log(INFO, "{0} icons loaded", ICONS.size());
    }, "icon-loader");
    thread.setDaemon(true);
    thread.start();
  }

  private Icons() {
  }

  public static void prefetch() {
    LOG.info("Prefetching icons");
  }

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
      var icon = ICONS.get(key);
      if (icon != null) {
        return icon(icon, size);
      }
      if (LATCH.getCount() != 0L) {
        try {
          LOG.log(INFO, "Waiting for {0}", key);
          if (!LATCH.await(1L, MINUTES)) {
            LOG.log(Level.SEVERE, "Unable to load icons in 1 minutes");
            return null;
          }
        } catch (InterruptedException e) {
          LOG.log(Level.SEVERE, "Interrupted", e);
          return null;
        }
      }
      icon = ICONS.get(key);
      return icon == null ? null : icon(icon, size);
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
