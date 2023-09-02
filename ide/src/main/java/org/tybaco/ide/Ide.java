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

import java.io.File;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.jar.JarFile;
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
    logger.info("Initializing classpath");
    var urls = libUrls();
    logger.info("Classpath initialized");
    updateSplash();
    var classLoader = new URLClassLoader("ide", urls, Thread.currentThread().getContextClassLoader());
    Thread.currentThread().setContextClassLoader(classLoader);
    logger.info("Preparing UI");
    bootstrapSplash(classLoader);
    logger.info("UI prepared");
    invokeMain(classLoader, args);
    logger.info("UI launched");
  }

  private static void bootstrapSplash(URLClassLoader classLoader) throws Exception {
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

  private static void invokeMain(URLClassLoader classLoader, String... args) throws Exception {
    var mainClass = classLoader.loadClass("org.tybaco.ui.Main");
    var mainMethod = mainClass.getMethod("main", String[].class);
    mainMethod.invoke(null, (Object) args);
  }

  private static URL[] libUrls() throws Exception {
    var classifier = classifier();
    var excludedLibs = new HashMap<String, String>(32, 0.5f);
    var list = new LinkedList<URL>();
    try (var jarFile = new JarFile(new File(Ide.class.getProtectionDomain().getCodeSource().getLocation().toURI()), false)) {
      var manifest = jarFile.getManifest();
      var classPath = manifest.getMainAttributes().getValue("Class-Path");
      for (var entry : classPath.split(" ")) {
        if (entry.startsWith("lib/") && entry.endsWith(".jar")) {
          excludedLibs.put(entry.substring("lib/".length()), null);
          var i = entry.indexOf("/javafx-");
          if (i > 0) {
            var j = entry.indexOf('-', i + "/javafx-".length());
            var artifact = entry.substring(i + 1, j);
            var version = entry.substring(j + 1, entry.length() - ".jar".length());
            var base = "https://repo.maven.apache.org/maven2/org/openjfx/" + artifact;
            var fxUrl = base + "/" + version + "/" + artifact + "-" + version + "-" + classifier + ".jar";
            var url = URL.of(new URI(fxUrl), null);
            list.addLast(url);
          }
        }
      }
    }
    var libPath = Path.of(Ide.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent().resolve("lib");
    try (var ds = Files.newDirectoryStream(libPath, "*.jar")) {
      for (var lib : ds) {
        if (!excludedLibs.containsKey(lib.getFileName().toString())) {
          list.addLast(lib.toUri().toURL());
        }
      }
    }
    return list.toArray(URL[]::new);
  }

  private static String classifier() {
    var osArch = System.getProperty("os.arch");
    var osName = System.getProperty("os.name");
    if (osName.equalsIgnoreCase("linux")) {
      return osArch.equals("aarch64") ? "linux-aarch64" : "linux";
    } else if (osName.equalsIgnoreCase("windows")) {
      return "win";
    } else {
      return osArch.equals("aarch64") ? "mac-aarch64" : "mac";
    }
  }

  private static void initLogging() {
    setProperty("java.util.logging.manager", LoggingManager.class.getName());
    var rootLogger = getLogManager().getLogger("");
    rootLogger.addHandler(new FastConsoleHandler());
  }
}
