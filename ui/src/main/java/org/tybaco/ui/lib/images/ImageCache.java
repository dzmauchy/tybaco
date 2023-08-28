package org.tybaco.ui.lib.images;

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

import lombok.extern.java.Log;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.LogRecord;

import static java.awt.Image.SCALE_SMOOTH;
import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.util.logging.Level.WARNING;
import static org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_HEIGHT;
import static org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_MAX_HEIGHT;
import static org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_MAX_WIDTH;
import static org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_WIDTH;

@Log
public class ImageCache {

  private static final ConcurrentHashMap<String, Image> CACHE = new ConcurrentHashMap<>(32, 0.5f);
  private static final ConcurrentHashMap<ImageKey, Image> SIZED_CACHE = new ConcurrentHashMap<>(32, 0.5f);
  private static final ConcurrentHashMap<IconKey, ImageIcon> ICON_CACHE = new ConcurrentHashMap<>(32, 0.5f);
  private static final ConcurrentHashMap<ImageKey, Image> SVG_IMAGE_CACHE = new ConcurrentHashMap<>(32, 0.5f);
  private static final ConcurrentHashMap<IconKey, ImageIcon> SVG_ICON_CACHE = new ConcurrentHashMap<>(32, 0.5f);

  private ImageCache() {
  }

  private static Image createImage(String path) {
    var classLoader = Thread.currentThread().getContextClassLoader();
    try (var is = classLoader.getResourceAsStream(path)) {
      if (is == null) {
        log.log(WARNING, "Unable to find an image {0}", path);
      } else {
        return getDefaultToolkit().createImage(is.readAllBytes());
      }
    } catch (IOException e) {
      var r = new LogRecord(WARNING, "Unable to find an image {0}");
      r.setParameters(new Object[]{path});
      r.setThrown(e);
      log.log(r);
    }
    return null;
  }

  private static BufferedImage defaultImage(int w, int h) {
    var image = new BufferedImage(w, h, TYPE_INT_RGB);
    var g = image.createGraphics();
    try {
      g.setBackground(Color.GRAY);
      g.clearRect(0, 0, w, h);
    } finally {
      g.dispose();
    }
    return image;
  }

  public static Image image(String path) {
    return CACHE.computeIfAbsent(path, p -> {
      var img = createImage(p);
      return img == null ? defaultImage(32, 32) : img;
    });
  }

  public static Image image(String path, int w, int h) {
    return SIZED_CACHE.computeIfAbsent(new ImageKey(path, w, h), k -> {
      var img = createImage(k.path);
      return img == null ? defaultImage(k.w, k.h) : img.getScaledInstance(k.w, k.h, SCALE_SMOOTH);
    });
  }

  public static Image image(String path, int size) {
    return image(path, size, size);
  }

  public static ImageIcon icon(String path, int size) {
    return ICON_CACHE.computeIfAbsent(new IconKey(path, size), k -> new ImageIcon(image(k.path, k.size)));
  }

  public static Image svgImage(String path, int w, int h) {
    return SVG_IMAGE_CACHE.computeIfAbsent(new ImageKey(path, w, h), k -> {
      var transcoder = new Svg2ImageTranscoder();
      transcoder.addTranscodingHint(KEY_WIDTH, (float) w);
      transcoder.addTranscodingHint(KEY_HEIGHT, (float) h);
      transcoder.addTranscodingHint(KEY_MAX_WIDTH, (float) w);
      transcoder.addTranscodingHint(KEY_MAX_HEIGHT, (float) h);
      var classLoader = Thread.currentThread().getContextClassLoader();
      try (var is = classLoader.getResourceAsStream(path)) {
        if (is != null) {
          var input = new TranscoderInput(is);
          transcoder.transcode(input, null);
          if (transcoder.image != null) {
            return transcoder.image;
          }
        }
      } catch (IOException | TranscoderException e) {
        var r = new LogRecord(WARNING, "Unable to find an image {0}");
        r.setParameters(new Object[]{path});
        r.setThrown(e);
        log.log(r);
      }
      return defaultImage(w, h);
    });
  }

  public static Image svgImage(String path, int size) {
    return svgImage(path, size, size);
  }

  public static ImageIcon svgIcon(String path, int size) {
    return SVG_ICON_CACHE.computeIfAbsent(new IconKey(path, size), k -> new ImageIcon(svgImage(path, size)));
  }

  public static ImageIcon smartIcon(String path, int size) {
    return path.endsWith(".svg") ? svgIcon(path, size) : icon(path, size);
  }

  private record ImageKey(String path, int w, int h) {
  }

  private record IconKey(String path, int size) {
  }

  private static final class Svg2ImageTranscoder extends ImageTranscoder {

    private BufferedImage image;

    @Override
    public BufferedImage createImage(int width, int height) {
      return new BufferedImage(width, height, TYPE_INT_ARGB);
    }

    @Override
    public void writeImage(BufferedImage img, TranscoderOutput output) {
      image = img;
    }
  }
}
