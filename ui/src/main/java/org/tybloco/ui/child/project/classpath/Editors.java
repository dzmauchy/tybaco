package org.tybloco.ui.child.project.classpath;

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

import javafx.beans.property.SimpleObjectProperty;
import org.springframework.stereotype.Component;
import org.tybloco.editors.model.BlockLib;
import org.tybloco.editors.model.ConstLib;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.jar.JarInputStream;

import static org.tybloco.logging.Log.error;

@Component
public final class Editors {

  public final SimpleObjectProperty<List<? extends ConstLib>> constLibs = new SimpleObjectProperty<>(this, "constLibs");
  public final SimpleObjectProperty<List<? extends BlockLib>> blockLibs = new SimpleObjectProperty<>(this, "blockLibs");

  public Editors(ProjectClasspath classpath) {
    classpath.addListener(o -> {
      if (classpath.getClassLoader() instanceof URLClassLoader c) {
        constLibs.set(null);
        blockLibs.set(null);
        update(c);
      }
    });
  }

  private void update(URLClassLoader classLoader) {
    var result = new LoadResult();
    result.process(classLoader.getURLs(), classLoader);
    blockLibs.set(result.blockLibs.values().stream().toList());
    constLibs.set(List.of());
  }

  private static final class LoadResult {

    private final ConcurrentSkipListMap<String, ReflectionBlockLib> blockLibs = new ConcurrentSkipListMap<>();

    private void process(URL[] urls, URLClassLoader classLoader) {
      Arrays.stream(urls).parallel().forEach(url -> {
        try {
          process(url, classLoader);
        } catch (Exception e) {
          error(Editors.class, "{0} processing error", e, url);
        }
      });
    }

    private void process(URL url, URLClassLoader classLoader) throws Exception {
      try (var is = new JarInputStream(url.openStream(), false)) {
        var types = new ConcurrentLinkedQueue<Class<?>>();
        for (var e = is.getNextJarEntry(); e != null; e = is.getNextJarEntry()) {
          var name = e.getName();
          if (name.endsWith(".class") && !name.contains("-") && !name.contains("$")) {
            var className = name.substring(0, name.length() - ".class".length()).replace('/', '.');
            try {
              var t = Class.forName(className, false, classLoader);
              if (t.getPackage() != null) {
                types.add(t);
              }
            } catch (Throwable throwable) {
              error(Editors.class, "Unable to load {0}", throwable, className);
            }
          }
        }
        types.forEach(t -> {
          for (var ann : t.getAnnotations()) {
            switch (ann.annotationType().getName()) {
              case "org.tybloco.runtime.meta.Constants" -> {
                processConstants(t, ann, classLoader);
                return;
              }
              case "org.tybloco.runtime.meta.Blocks" -> {
                processBlocks(t, ann, classLoader);
                return;
              }
            }
          }
          for (var c : t.getConstructors()) {
            for (var ann : c.getAnnotations()) {
              if (ann.annotationType().getName().equals("org.tybloco.runtime.meta.Block")) {
                processBlock(c, ann, null, classLoader);
                return;
              }
            }
          }
        });
      }
    }

    private void processBlocks(Class<?> type, Annotation ta, ClassLoader classLoader) {
      for (var m : type.getMethods()) {
        if (!Modifier.isStatic(m.getModifiers())) continue;
        for (var a : m.getAnnotations()) {
          if (a.annotationType().getName().equals("org.tybloco.runtime.meta.Block")) {
            processBlock(m, a, ta, classLoader);
            break;
          }
        }
      }
    }

    private ReflectionBlockLib libForPkg(Package pkg, ClassLoader classLoader, ConcurrentSkipListMap<String, ReflectionBlockLib> libs) {
      if (pkg == null) return null;
      var index = pkg.getName().lastIndexOf('.');
      final ReflectionBlockLib parentLib;
      if (index >= 0) {
        var parentPackageName = pkg.getName().substring(0, index);
        var parentPkg = classLoader.getDefinedPackage(parentPackageName);
        parentLib = libForPkg(parentPkg, classLoader, libs);
      } else {
        parentLib = null;
      }
      for (var a : pkg.getAnnotations()) {
        if (a.annotationType().getName().equals("org.tybloco.runtime.meta.Lib")) {
          return parentLib == null
            ? libs.computeIfAbsent(pkg.getName(), p -> new ReflectionBlockLib(p, a))
            : parentLib.libs.computeIfAbsent(pkg.getName(), p -> new ReflectionBlockLib(p, a));
        }
      }
      return parentLib;
    }

    private void processBlock(Executable executable, Annotation ea, Annotation ta, ClassLoader classLoader) {
      var pl = libForPkg(executable.getDeclaringClass().getPackage(), classLoader, blockLibs);
      if (pl == null) return;
      var id = executable instanceof Method m ? m.getDeclaringClass().getName() + "." + m.getName() : executable.getDeclaringClass().getName();
      if (ta == null) {
        pl.blocks.computeIfAbsent(id, i -> new ReflectionLibBlock(i, executable, ea));
      } else {
        pl.libs
          .computeIfAbsent(pl.id() + "_" + executable.getDeclaringClass().getName(), k -> new ReflectionBlockLib(k, ta))
          .blocks
          .computeIfAbsent(id, i -> new ReflectionLibBlock(i, executable, ea));
      }
    }

    private void processConstants(Class<?> type, Annotation ta, ClassLoader classLoader) {

    }
  }
}
