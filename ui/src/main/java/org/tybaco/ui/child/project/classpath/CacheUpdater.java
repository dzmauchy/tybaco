package org.tybaco.ui.child.project.classpath;

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

import jakarta.annotation.PostConstruct;
import javafx.beans.value.ChangeListener;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;
import org.tybaco.ui.lib.repo.ArtifactClassPath;

import java.net.URLClassLoader;
import java.util.concurrent.*;
import java.util.jar.JarInputStream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.logging.Level.SEVERE;
import static org.tybaco.ui.lib.logging.Logging.LOG;

@Component
public final class CacheUpdater implements AutoCloseable {

  private final ProjectClasspath classpath;
  private final ProjectCache cache;
  private final ChangeListener<ArtifactClassPath> classPathListener;

  public CacheUpdater(ProjectClasspath classpath, ProjectCache cache) {
    this.classpath = classpath;
    this.cache = cache;
    this.classPathListener = (o, ov, nv) -> onClassPathChange(nv);
  }

  @PostConstruct
  public void init() {
    classpath.classPath.addListener(classPathListener);
  }

  private void onClassPathChange(ArtifactClassPath classPath) {
    cache.clear();
    if (classPath == null) {
      return;
    }
    var classLoader = classPath.classLoader;
    var urls = classLoader.getURLs();
    if (urls.length == 0) {
      return;
    }
    var threadFactory = new CustomizableThreadFactory();
    threadFactory.setThreadGroup(classpath.project.threadGroup);
    threadFactory.setDaemon(true);
    try (var pool = new ThreadPoolExecutor(6, 6, 0L, SECONDS, new LinkedTransferQueue<>(), threadFactory)) {
      pool.allowCoreThreadTimeOut(true);
      for (var url : urls) {
        pool.execute(() -> {
          try (var jis = new JarInputStream(url.openStream())) {
            processUrl(jis, classLoader);
          } catch (Throwable e) {
            LOG.log(SEVERE, "Unable to process " + url, e);
          }
        });
      }
    }
  }

  private void processUrl(JarInputStream jis, URLClassLoader classLoader) throws Exception {
    for (var e = jis.getNextJarEntry(); e != null; e = jis.getNextJarEntry()) {
      if (e.isDirectory()) {
        continue;
      }
      if (e.getName().endsWith(".class")) {
        var name = e.getName().substring(0, e.getName().length() - ".class".length()).replace('/', '.');
        try {
          var type = Class.forName(name, false, classLoader);
          for (var ta : type.getAnnotations()) {
            switch (ta.annotationType().getName()) {
              case "org.tybaco.runtime.annotation.ConstantFactory" -> cache.constants.put(type, Boolean.TRUE);
              case "org.tybaco.runtime.annotation.BlockFactory" -> {
                for (var method : type.getMethods()) {
                  for (var ma : method.getAnnotations()) {
                    if ("org.tybaco.runtime.annotation.Block".equals(ma.annotationType().getName())) {
                      cache.blocks.computeIfAbsent(type, k -> new ConcurrentLinkedQueue<>()).add(method);
                      break;
                    }
                  }
                }
              }
              case "org.tybaco.runtime.annotation.Block" -> {
                for (var method : type.getMethods()) {
                  for (var ma : method.getAnnotations()) {
                    if ("org.tybaco.runtime.annotation.Input".equals(ma.annotationType().getName())) {
                      cache.inputs.computeIfAbsent(type, k -> new ConcurrentLinkedQueue<>()).add(method);
                      break;
                    }
                  }
                }
              }
            }
          }
        } catch (Throwable x) {
          LOG.log(SEVERE, "Unable to load " + name, x);
        }
      }
    }
  }

  @Override
  public void close() {
    classpath.classPath.removeListener(classPathListener);
  }
}
