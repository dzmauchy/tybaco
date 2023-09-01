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

import org.springframework.context.support.GenericApplicationContext;
import org.tybaco.logging.FastConsoleHandler;
import org.tybaco.logging.LoggingManager;
import org.tybaco.ui.lib.logging.UILogHandler;
import org.tybaco.ui.main.MainApplicationContext;

import static java.awt.EventQueue.invokeLater;
import static java.lang.System.setProperty;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.LogManager.getLogManager;
import static java.util.logging.Logger.getLogger;
import static org.tybaco.ui.splash.Splash.renderSplash;
import static org.tybaco.ui.splash.Splash.updateSplash;
import static org.tybaco.ui.splash.SplashStatus.updateSplashStatus;

public final class Main {

  public static void main(String... args) {
    renderSplash();
    initLogging();
    var ctx = new MainApplicationContext();
    updateLaf();
    invokeLater(() -> bootstrap(ctx));
  }

  private static void bootstrap(GenericApplicationContext context) {
    updateSplash();
    try {
      context.refresh();
      updateSplash();
      updateSplash();
      updateSplashStatus();
    } catch (Throwable e) {
      try (context) {
        context.stop();
      } catch (Throwable x) {
        e.addSuppressed(x);
      }
      getLogger("main").log(SEVERE, "Bootstrap error", e);
    }
  }

  private static void initLogging() {
    setProperty("java.util.logging.manager", LoggingManager.class.getName());
    var rootLogger = getLogManager().getLogger("");
    rootLogger.addHandler(new FastConsoleHandler());
    rootLogger.addHandler(new UILogHandler());
    updateSplash();
  }

  private static void updateLaf() {
    updateSplash();
  }
}
