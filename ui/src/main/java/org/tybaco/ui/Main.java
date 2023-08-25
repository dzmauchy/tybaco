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
import org.tybaco.ui.main.MainConfiguration;
import org.tybaco.ui.main.MainFrame;

import java.awt.*;
import java.io.IOException;
import java.util.logging.LogManager;

import static java.awt.EventQueue.invokeLater;
import static java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_RENDERING;
import static java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_RENDER_QUALITY;
import static java.lang.System.setProperty;
import static java.util.logging.Level.INFO;

public final class Main implements ApplicationListener<ApplicationEvent> {

  public static void main(String... args) {
    var splash = SplashScreen.getSplashScreen();
    updateSplash(splash);
    initLogging();
    updateSplash(splash, SplashStatus.INIT_LOGGING);
    var ctx = new AnnotationConfigApplicationContext();
    var latch = new Latch(1);
    invokeLater(() -> bootstrap(splash, latch, ctx));
    updateSplash(splash, SplashStatus.EVENT_QUEUE);
    try {
      ctx.setId("root");
      ctx.setDisplayName("TybacoIDE");
      ctx.setClassLoader(Thread.currentThread().getContextClassLoader());
      ctx.setAllowCircularReferences(false);
      ctx.setAllowBeanDefinitionOverriding(false);
      ctx.addApplicationListener(new Main());
      ctx.register(MainConfiguration.class);
      updateSplash(splash, SplashStatus.CONTEXT_CONFIGURED);
      FlatDarculaLaf.installLafInfo();
      FlatDarculaLaf.setup();
      updateSplash(splash, SplashStatus.LAF_CONFIGURED);
    } finally {
      latch.releaseShared(1);
    }
  }

  private static void bootstrap(SplashScreen splash, Latch latch, GenericApplicationContext context) {
    latch.acquireShared(1);
    updateSplash(splash, SplashStatus.UI_THREAD_CREATED);
    context.refresh();
    updateSplash(splash, SplashStatus.CONTEXT_REFRESHED);
    var mainFrame = context.getBean(MainFrame.class);
    assert mainFrame != null;
    updateSplash(splash, SplashStatus.MAIN_FRAME_PREPARED);
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
    updateSplash(splashScreen, SplashStatus.BOOTSTRAP);
    var classPath = Thread.currentThread().getContextClassLoader();
    var newUrl = classPath.getResource("images/logo.jpg");
    if (newUrl != null) {
      try {
        splashScreen.setImageURL(newUrl);
      } catch (IOException e) {
        e.printStackTrace(System.err);
      }
    }
    updateSplash(splashScreen, SplashStatus.FIRST_UPDATE);

    var g = splashScreen.createGraphics();
    try {
      g.setFont(createFont("fonts/wf.otf", 72));
      updateSplash(splashScreen, SplashStatus.LOAD_FONT1);
      g.setColor(Color.WHITE);
      g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
      g.setRenderingHint(KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_QUALITY);
      g.setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);
      var title = "Tybaco IDE";
      var vector = g.getFont().createGlyphVector(g.getFontRenderContext(), title);
      var bounds = vector.getVisualBounds();
      var x = 20;
      var y = (int) (bounds.getHeight() + 20d);
      g.drawGlyphVector(vector, x, y);
      g.setColor(Color.DARK_GRAY);
      g.setStroke(new BasicStroke(2f));
      g.draw(vector.getOutline(x, y));
      y += 30;
      g.setColor(Color.WHITE);
      g.drawLine(x, y, 700, y);
      g.setFont(createFont("fonts/fz.ttf", 36));
      updateSplash(splashScreen, SplashStatus.LOAD_FONT2);
      vector = g.getFont().createGlyphVector(g.getFontRenderContext(), "A microservice visual IDE");
      y += (int) (vector.getVisualBounds().getHeight() + 10);
      g.drawGlyphVector(vector, x, y);
      g.setColor(Color.DARK_GRAY);
      g.setStroke(new BasicStroke(1f));
      g.draw(vector.getOutline(x, y));
    } finally {
      g.dispose();
      splashScreen.update();
      updateSplash(splashScreen, SplashStatus.SECOND_UPDATE);
    }
  }

  private static Font createFont(String resource, int size) {
    try (var is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
      if (is != null) {
        return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(Font.BOLD, size);
      }
    } catch (IOException | FontFormatException e) {
      e.printStackTrace(System.err);
    }
    return new Font(Font.SANS_SERIF, Font.BOLD, size);
  }

  private static void updateSplash(SplashScreen splashScreen, SplashStatus status) {
    if (splashScreen == null) {
      return;
    }
    var b = splashScreen.getBounds();
    var g = splashScreen.createGraphics();
    var statuses = SplashStatus.values();
    try {
      g.setBackground(Color.WHITE);
      var w = (b.width / statuses.length) * (status.ordinal() + 1);
      var y = b.height - 10;
      g.clearRect(0, y, w, y + 10);
    } finally {
      g.dispose();
      splashScreen.update();
    }
  }

  private enum SplashStatus {
    BOOTSTRAP,
    FIRST_UPDATE,
    LOAD_FONT1,
    LOAD_FONT2,
    SECOND_UPDATE,
    INIT_LOGGING,
    EVENT_QUEUE,
    CONTEXT_CONFIGURED,
    LAF_CONFIGURED,
    UI_THREAD_CREATED,
    CONTEXT_REFRESHED,
    MAIN_FRAME_PREPARED
  }
}
