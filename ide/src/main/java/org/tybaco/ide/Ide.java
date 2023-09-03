package org.tybaco.ide;

/*-
 * #%L
 * ide
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

import org.tybaco.ide.splash.SplashPreloader;
import org.tybaco.ide.splash.SplashStatus;
import org.tybaco.logging.FastConsoleHandler;
import org.tybaco.logging.LoggingManager;
import org.tybaco.ui.Main;
import org.tybaco.ui.splash.SplashBeanPostProcessor;

import static java.lang.System.setProperty;
import static java.util.logging.LogManager.getLogManager;

public class Ide {

  public static void main(String... args) {
    initLogging();
    System.setProperty("javafx.preloader", SplashPreloader.class.getName());
    Main.updateSplash = SplashStatus::incrementStep;
    Main.updateSplashStatus = SplashStatus::updateSplashStatus;
    SplashBeanPostProcessor.incrementStep = SplashStatus::incrementStep;
    Main.main(args);
  }

  private static void initLogging() {
    setProperty("java.util.logging.manager", LoggingManager.class.getName());
    var rootLogger = getLogManager().getLogger("");
    rootLogger.addHandler(new FastConsoleHandler());
  }
}
