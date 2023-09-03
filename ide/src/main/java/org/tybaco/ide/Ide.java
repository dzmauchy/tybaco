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
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarFile;
import java.util.logging.LogManager;

import static java.lang.System.setProperty;
import static java.nio.file.Files.copy;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
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
    logger.log(INFO, "Classpath initialized: {0}", List.of(urls));
    updateSplash();
    var classLoader = classLoader(urls);
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
    var excludedLibs = new TreeMap<String, String>();
    try (var jarFile = new JarFile(new File(Ide.class.getProtectionDomain().getCodeSource().getLocation().toURI()), false)) {
      var manifest = jarFile.getManifest();
      var classPath = manifest.getMainAttributes().getValue("Class-Path");
      for (var entry : classPath.split(" ")) {
        if (entry.startsWith("lib/") && entry.endsWith(".jar")) {
          excludedLibs.put(entry.substring("lib/".length()), null);
        }
      }
    }
    var libPath = Path.of(Ide.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent().resolve("lib");
    var list = new LinkedList<URL>();
    try (var ds = Files.newDirectoryStream(libPath, "*.jar")) {
      var classifier = classifier();
      var logger = LogManager.getLogManager().getLogger("");
      try (var executor = executor()) {
        var futures = new LinkedList<Future<URL>>();
        for (var lib : ds) {
          var fn = lib.getFileName().toString();
          if (!excludedLibs.containsKey(fn)) {
            list.addFirst(lib.toUri().toURL());
          }
          if (fn.startsWith("javafx-")) {
            futures.add(executor.submit(() -> {
              var j = fn.indexOf('-', "javafx-".length() + 1);
              var artifact = fn.substring(0, j);
              var version = fn.substring(j + 1, fn.length() - 4);
              var base = "https://maven-central.storage.googleapis.com/maven2/org/openjfx/" + artifact;
              var fxUrl = base + "/" + version + "/" + artifact + "-" + version + "-" + classifier + ".jar";
              var url = URL.of(new URI(fxUrl), null);
              var tempFile = Files.createTempFile("fx", ".jar");
              try (var is = url.openStream()) {
                copy(is, tempFile, REPLACE_EXISTING);
              }
              logger.log(INFO, "{0} copied", url);
              return tempFile.toUri().toURL();
            }));
          }
        }
        for (var future : futures) {
          try {
            list.addLast(future.get());
          } catch (Exception e) {
            logger.log(SEVERE, "I/O error", e);
          }
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

  private static ThreadPoolExecutor executor() {
    var counter = new AtomicInteger();
    var threadGroup = new ThreadGroup("fx-libs");
    var threadFactory = (ThreadFactory) r -> {
      var thread = new Thread(threadGroup, r, "t" + counter.getAndIncrement());
      thread.setDaemon(true);
      return thread;
    };
    var policy = new ThreadPoolExecutor.CallerRunsPolicy();
    var executor = new ThreadPoolExecutor(16, 16, 1L, SECONDS, new SynchronousQueue<>(), threadFactory, policy);
    executor.allowCoreThreadTimeOut(true);
    return executor;
  }

  private static URLClassLoader classLoader(URL[] urls) {
    var logger = LogManager.getLogManager().getLogger("");
    var classLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
    Thread.currentThread().setContextClassLoader(classLoader);
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      var closeFiles = (AutoCloseable) () -> {
        var location = Ide.class.getProtectionDomain().getCodeSource().getLocation();
        var mainJarDir = Path.of(location.toURI()).getParent();
        for (var url : urls) {
          var path = Path.of(url.toURI());
          if (!path.startsWith(mainJarDir)) {
            if (Files.deleteIfExists(path)) {
              logger.log(INFO, "Deleted {0}", path);
            }
          }
        }
      };
      try (closeFiles) {
        classLoader.close();
      } catch (Throwable e) {
        logger.log(SEVERE, "I/O error", e);
      }
    }));
    return classLoader;
  }
}
