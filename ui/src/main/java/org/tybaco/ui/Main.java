package org.tybaco.ui;

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

import com.formdev.flatlaf.FlatDarculaLaf;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.tybaco.logging.FastConsoleHandler;
import org.tybaco.logging.LoggingManager;
import org.tybaco.ui.lib.logging.UILogHandler;
import org.tybaco.ui.lib.utils.Latch;
import org.tybaco.ui.lib.utils.ThreadUtils;
import org.tybaco.ui.main.MainConfiguration;
import org.tybaco.ui.main.MainFrame;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.logging.LogManager;

import static java.awt.Color.LIGHT_GRAY;
import static java.awt.Color.WHITE;
import static java.awt.EventQueue.invokeLater;
import static java.awt.Font.BOLD;
import static java.awt.Font.PLAIN;
import static java.awt.RenderingHints.*;
import static java.lang.System.setProperty;
import static java.util.logging.Level.INFO;
import static org.jfree.chart.ChartColor.LIGHT_BLUE;
import static org.tybaco.ui.Main.SplashStatus.*;
import static org.tybaco.ui.lib.utils.ThreadUtils.tccl;

public final class Main implements ApplicationListener<ApplicationEvent> {

  public static void main(String... args) {
    var splash = SplashScreen.getSplashScreen();
    updateSplash(splash);
    initLogging();
    updateSplash(splash, INIT_LOGGING);
    var ctx = new AnnotationConfigApplicationContext();
    var latch = new Latch(1);
    invokeLater(() -> bootstrap(splash, latch, ctx));
    updateSplash(splash, EVENT_QUEUE_INITIALIZED);
    try {
      ctx.setId("root");
      ctx.setDisplayName("TybacoIDE");
      ctx.setClassLoader(tccl());
      ctx.setAllowCircularReferences(false);
      ctx.setAllowBeanDefinitionOverriding(false);
      ctx.addApplicationListener(new Main());
      ctx.register(MainConfiguration.class);
      updateSplash(splash, CONTEXT_CONFIGURED);
      FlatDarculaLaf.installLafInfo();
      FlatDarculaLaf.setup();
      updateSplash(splash, LAF_CONFIGURED);
    } finally {
      latch.releaseShared(1);
    }
  }

  private static void bootstrap(SplashScreen splash, Latch latch, GenericApplicationContext context) {
    latch.acquireShared(1);
    updateSplash(splash, UI_THREAD_CREATED);
    context.refresh();
    updateSplash(splash, CONTEXT_REFRESHED);
    var mainFrame = context.getBean(MainFrame.class);
    assert mainFrame != null;
    updateSplash(splash, MAIN_FRAME_PREPARED);
    mainFrame.setVisible(true);
  }

  private static void initLogging() {
    setProperty("java.util.logging.manager", LoggingManager.class.getName());
    var rootLogger = LogManager.getLogManager().getLogger("");
    rootLogger.addHandler(new FastConsoleHandler());
    rootLogger.addHandler(new UILogHandler());
  }

  @Override
  public void onApplicationEvent(ApplicationEvent event) {
    LogManager.getLogManager().getLogger("").log(INFO, "{0}", event);
  }

  private static void updateSplash(SplashScreen splashScreen) {
    if (splashScreen == null) {
      return;
    }
    updateSplash(splashScreen, BOOTSTRAPPED);
    var newUrl = tccl().getResource("images/logo.jpg");
    if (newUrl != null) {
      try {
        splashScreen.setImageURL(newUrl);
      } catch (IOException e) {
        e.printStackTrace(System.err);
      }
    }
    updateSplash(splashScreen, FIRST_UPDATE);

    var g = splashScreen.createGraphics();
    try {
      g.setFont(createFont("fonts/hs.ttf", 80));
      updateSplash(splashScreen, FONT1_LOADED);
      g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
      g.setRenderingHint(KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_QUALITY);
      g.setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);
      int x = 20, y = 20;
      var bounds = drawOutline(g, "Tybaco IDE", x, y, 2f);
      y += 30 + (int) bounds.getHeight();
      g.setColor(WHITE);
      g.drawLine(x, y, 700, y);
      y += 20;
      g.setFont(g.getFont().deriveFont(48f));
      updateSplash(splashScreen, FONT2_LOADED);
      drawOutline(g, "A microservice visual IDE", x, y, 1f);
    } finally {
      g.dispose();
      splashScreen.update();
      updateSplash(splashScreen, SECOND_UPDATE);
    }
  }

  private static Rectangle2D drawOutline(Graphics2D g, String text, int x, int y, float stroke) {
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

  private static void updateSplash(SplashScreen splashScreen, SplashStatus status) {
    if (splashScreen == null) {
      return;
    }
    var b = splashScreen.getBounds();
    var g = splashScreen.createGraphics();
    var statuses = SplashStatus.values();
    try {
      g.setBackground(LIGHT_BLUE);
      var w = (b.width / statuses.length) * (status.ordinal() + 1);
      var y = b.height - 7;
      g.clearRect(0, y, w, y + 7);
    } finally {
      g.dispose();
      splashScreen.update();
    }
  }

  enum SplashStatus {
    BOOTSTRAPPED,
    FIRST_UPDATE,
    FONT1_LOADED,
    FONT2_LOADED,
    SECOND_UPDATE,
    INIT_LOGGING,
    EVENT_QUEUE_INITIALIZED,
    CONTEXT_CONFIGURED,
    LAF_CONFIGURED,
    UI_THREAD_CREATED,
    CONTEXT_REFRESHED,
    MAIN_FRAME_PREPARED
  }
}
