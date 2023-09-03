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

import org.tybaco.ide.splash.Splash;
import org.tybaco.ide.splash.SplashStatus;
import org.tybaco.logging.FastConsoleHandler;
import org.tybaco.logging.LoggingManager;

import java.util.logging.LogManager;

import static java.lang.System.setProperty;
import static java.util.logging.LogManager.getLogManager;
import static org.tybaco.ide.splash.Splash.renderSplash;
import static org.tybaco.ide.splash.Splash.updateSplash;

public class Ide {

  public static void main(String... args) throws Exception {
    renderSplash();
    initLogging();
    var logger = LogManager.getLogManager().getLogger("");
    updateSplash();
    logger.info("Preparing UI");
    bootstrapSplash(Thread.currentThread().getContextClassLoader());
    logger.info("UI prepared");
    invokeMain(Thread.currentThread().getContextClassLoader(), args);
    logger.info("UI launched");
  }

  private static void bootstrapSplash(ClassLoader classLoader) throws Exception {
    updateSplash();
    var mainClass = classLoader.loadClass("org.tybaco.ui.Main");
    var updateSplash = mainClass.getField("updateSplash");
    updateSplash.set(null, (Runnable) Splash::updateSplash);
    var updateSplashStatus = mainClass.getField("updateSplashStatus");
    updateSplashStatus.set(null, (Runnable) SplashStatus::updateSplashStatus);
    var splashBeanPostProcessor = classLoader.loadClass("org.tybaco.ui.splash.SplashBeanPostProcessor");
    var incrementStep = splashBeanPostProcessor.getField("incrementStep");
    incrementStep.set(null, (Runnable) SplashStatus::incrementStep);
    var processorUpdateSplash = splashBeanPostProcessor.getField("updateSplash");
    processorUpdateSplash.set(null, (Runnable) () -> Splash.updateSplash(false));
  }

  private static void invokeMain(ClassLoader classLoader, String... args) throws Exception {
    var mainClass = classLoader.loadClass("org.tybaco.ui.Main");
    var mainMethod = mainClass.getMethod("main", String[].class);
    mainMethod.invoke(null, (Object) args);
  }

  private static void initLogging() {
    setProperty("java.util.logging.manager", LoggingManager.class.getName());
    var rootLogger = getLogManager().getLogger("");
    rootLogger.addHandler(new FastConsoleHandler());
  }
}
