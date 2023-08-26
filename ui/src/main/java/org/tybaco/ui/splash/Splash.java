package org.tybaco.ui.splash;

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

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import static java.awt.Color.LIGHT_GRAY;
import static java.awt.Color.WHITE;
import static java.awt.Font.BOLD;
import static java.awt.Font.PLAIN;
import static java.awt.RenderingHints.*;
import static org.jfree.chart.ChartColor.LIGHT_BLUE;
import static org.tybaco.ui.lib.utils.ThreadUtils.tccl;
import static org.tybaco.ui.splash.SplashStatus.*;

public final class Splash {

  private Splash() {
  }

  public static void renderSplash() {
    var splashScreen = SplashScreen.getSplashScreen();
    if (splashScreen == null) {
      return;
    }
    updateSplash();
    var newUrl = tccl().getResource("images/logo.jpg");
    if (newUrl != null) {
      try {
        splashScreen.setImageURL(newUrl);
      } catch (IOException e) {
        e.printStackTrace(System.err);
      }
    }
    updateSplash();

    var g = splashScreen.createGraphics();
    try {
      g.setFont(createFont("fonts/hs.ttf", 80));
      updateSplash();
      g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
      g.setRenderingHint(KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_QUALITY);
      g.setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);
      int x = 20, y = 20;
      var bounds = drawText(g, "Tybaco IDE", x, y);
      y += 30 + (int) bounds.getHeight();
      g.setColor(WHITE);
      g.drawLine(x, y, 700, y);
      y += 20;
      g.setFont(g.getFont().deriveFont(48f));
      updateSplash();
      drawText(g, "A microservice visual IDE", x, y);
    } finally {
      g.dispose();
      splashScreen.update();
      updateSplash();
    }
  }

  private static Rectangle2D drawText(Graphics2D g, String text, int x, int y) {
    var vector = g.getFont().createGlyphVector(g.getFontRenderContext(), text);
    var bounds = vector.getVisualBounds();
    g.setColor(LIGHT_GRAY);
    g.drawGlyphVector(vector, x, y + (int) bounds.getHeight());
    return bounds;
  }

  private static Font createFont(String resource, int size) {
    try (var is = tccl().getResourceAsStream(resource)) {
      if (is != null) {
        return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(PLAIN, size);
      }
    } catch (IOException | FontFormatException e) {
      e.printStackTrace(System.err);
    }
    return new Font(Font.SANS_SERIF, BOLD, size);
  }

  public static void updateSplash() {
    updateSplash(true);
  }

  public static void updateSplash(boolean increment) {
    var splashScreen = SplashScreen.getSplashScreen();
    if (splashScreen == null) {
      return;
    }
    var b = splashScreen.getBounds();
    var g = splashScreen.createGraphics();
    try {
      g.setBackground(LIGHT_BLUE);
      var nextStep = increment ? incrementStep() : step();
      var maxStep = maxStep();
      var w = (b.width * nextStep) / maxStep;
      var y = b.height - 7;
      g.clearRect(0, y, w, y + 7);
    } finally {
      g.dispose();
      splashScreen.update();
    }
  }
}
