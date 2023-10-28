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

import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
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
import java.util.function.BiFunction;
import java.util.jar.JarInputStream;

import static org.tybloco.logging.Log.error;
import static org.tybloco.ui.child.project.classpath.ConstantEditors.libConst;

@Component
public final class Editors {

  public final SimpleObjectProperty<List<? extends ConstLib>> constLibs = new SimpleObjectProperty<>(this, "constLibs");
  public final SimpleObjectProperty<List<? extends BlockLib>> blockLibs = new SimpleObjectProperty<>(this, "blockLibs");

  public Editors(ProjectClasspath classpath) {
    classpath.addListener(o -> {
      if (classpath.getClassLoader() instanceof URLClassLoader c) {
        constLibs.set(List.of());
        blockLibs.set(List.of());
        update(c);
      }
    });
  }

  private void update(URLClassLoader classLoader) {
    var result = new LoadResult();
    result.process(classLoader.getURLs(), classLoader);
    blockLibs.set(result.blockLibs.values().stream().toList());
    constLibs.set(result.constLibs.values().stream().toList());
  }

  private static final class LoadResult {

    private final ConcurrentSkipListMap<String, ReflectionBlockLib> blockLibs = new ConcurrentSkipListMap<>();
    private final ConcurrentSkipListMap<String, ReflectionConstLib> constLibs = new ConcurrentSkipListMap<>();

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

    private <L extends ReflectionMetaLib<?, L>> L lib(Package p, ClassLoader cl, ConcurrentSkipListMap<String, L> libs, BiFunction<String, Annotation, L> lc) {
      if (p == null) return null;
      var index = p.getName().lastIndexOf('.');
      final L parentLib;
      if (index >= 0) {
        var parentPackageName = p.getName().substring(0, index);
        var parentPkg = cl.getDefinedPackage(parentPackageName);
        parentLib = lib(parentPkg, cl, libs, lc);
      } else {
        parentLib = null;
      }
      for (var a : p.getAnnotations()) {
        if (a.annotationType().getName().equals("org.tybloco.runtime.meta.Lib")) {
          return parentLib == null
            ? libs.computeIfAbsent(p.getName(), id -> lc.apply(id, a))
            : parentLib.libs.computeIfAbsent(p.getName(), id -> lc.apply(id, a));
        }
      }
      return parentLib;
    }

    private void processBlock(Executable executable, Annotation ea, Annotation ta, ClassLoader classLoader) {
      var pl = lib(executable.getDeclaringClass().getPackage(), classLoader, blockLibs, ReflectionBlockLib::new);
      if (pl == null) return;
      var id = executable instanceof Method m ? m.getDeclaringClass().getName() + "." + m.getName() : executable.getDeclaringClass().getName();
      if (ta == null) {
        pl.children.computeIfAbsent(id, i -> new ReflectionLibBlock(i, executable, ea));
      } else {
        pl.libs
          .computeIfAbsent(pl.id() + "_" + executable.getDeclaringClass().getName(), k -> new ReflectionBlockLib(k, ta))
          .children
          .computeIfAbsent(id, i -> new ReflectionLibBlock(i, executable, ea));
      }
    }

    private void processConstants(Class<?> type, Annotation ta, ClassLoader classLoader) {
      for (var m : type.getMethods()) {
        if (!Modifier.isStatic(m.getModifiers())) continue;
        if (m.getParameterCount() != 1 && m.getParameterCount() != 0) continue;
        for (var a : m.getAnnotations()) {
          if (a.annotationType().getName().equals("org.tybloco.runtime.meta.Constant")) {
            var id = m.getDeclaringClass().getName() + "." + m.getName();
            var pl = lib(m.getDeclaringClass().getPackage(), classLoader, constLibs, ReflectionConstLib::new);
            if (pl != null) {
              var children = pl.libs
                .computeIfAbsent(pl.id() + "_" + m.getDeclaringClass().getName(), k -> new ReflectionConstLib(k, ta))
                .children;
              if (m.getParameterCount() == 1) {
                children.computeIfAbsent(id, i -> libConst(i, m, a));
              } else {
                var t = m.getReturnType().getName();
                var e = new MethodCallExpr(new TypeExpr(new ClassOrInterfaceType(null, type.getName())), m.getName());
                children.computeIfAbsent(id, i -> new ReflectionConst(i, a, t, e));
              }
            }
            break;
          }
        }
      }
      for (var f : type.getFields()) {
        if (!Modifier.isStatic(f.getModifiers())) continue;
        for (var a : f.getAnnotations()) {
          if (a.annotationType().getName().equals("org.tybloco.runtime.meta.Constant")) {
            var id = f.getDeclaringClass().getName() + "." + f.getName();
            var pl = lib(f.getDeclaringClass().getPackage(), classLoader, constLibs, ReflectionConstLib::new);
            var t = f.getType().getName();
            var e = new FieldAccessExpr(new TypeExpr(new ClassOrInterfaceType(null, type.getName())), f.getName());
            pl.libs
              .computeIfAbsent(pl.id() + "_" + f.getDeclaringClass().getName(), k -> new ReflectionConstLib(k, ta))
              .children
              .computeIfAbsent(id, i -> new ReflectionConst(i, a, t, e));
            break;
          }
        }
      }
    }
  }
}
