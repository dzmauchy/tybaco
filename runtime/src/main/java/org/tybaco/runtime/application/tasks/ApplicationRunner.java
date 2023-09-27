package org.tybaco.runtime.application.tasks;

/*-
 * #%L
 * runtime
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

import org.tybaco.runtime.application.*;
import org.tybaco.runtime.application.tasks.run.*;
import org.tybaco.runtime.basic.Initializable;
import org.tybaco.runtime.basic.Startable;
import org.tybaco.runtime.exception.*;
import org.tybaco.runtime.reflect.ClassInfoCache;
import org.tybaco.runtime.reflect.ConstantInfoCache;
import org.tybaco.runtime.util.TransformedMap;

import java.lang.reflect.*;
import java.util.*;

import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;
import static org.tybaco.runtime.exception.BlockCloseException.wrapCloseable;
import static org.tybaco.runtime.util.IntSet.EMPTY_INT_SET;
import static org.tybaco.runtime.util.IntSet.tryAdd;

public class ApplicationRunner implements ApplicationTask {

  @Override
  public void run(ApplicationContext context) {
    requireNonNull(context.application, "Application is not loaded");
    var runtimeApp = requireNonNull(runtimeApp(context.application));
    context.closeable = runtimeApp::close;
  }

  RuntimeApp runtimeApp(Application app) {
    var resolver = new ApplicationResolver(app);
    try {
      for (var constant : app.constants()) resolver.resolveConstant(constant);
      for (var block : app.blocks()) resolver.resolveBlock(block, EMPTY_INT_SET);
      resolver.start();
      return resolver.runtimeApp;
    } catch (Throwable e) {
      try {
        resolver.runtimeApp.close();
      } catch (Throwable x) {
        e.addSuppressed(x);
      }
      throw e;
    }
  }

  private static final class ApplicationResolver {

    private final RuntimeApp runtimeApp = new RuntimeApp();
    private final ClassInfoCache classInfoCache = new ClassInfoCache();
    private final ConstantInfoCache constantInfoCache = new ConstantInfoCache();
    private final LinkedHashMap<ResolvableObject, Object> beans;
    private final IdentityHashMap<ApplicationBlock, TreeMap<String, TreeMap<Integer, Link>>> args;
    private final IdentityHashMap<ApplicationBlock, TreeMap<String, TreeMap<Integer, Link>>> inputs;
    private final IdentityHashMap<ResolvableObject, TreeMap<String, Object>> outValues;
    private final Resolvables objectMap;

    private ApplicationResolver(Application app) {
      this.beans = new LinkedHashMap<>(app.blocks().size() + app.constants().size(), 1f);
      this.args = new IdentityHashMap<>(app.blocks().size());
      this.inputs = new IdentityHashMap<>(app.blocks().size());
      this.outValues = new IdentityHashMap<>(app.blocks().size() + app.constants().size());
      this.objectMap = new Resolvables(app.blocks(), app.constants());

      app.links().forEach(l -> {
        var out = objectMap.get(l.out().block());
        if (out == null) throw new InvalidLinkException(l, "non-existing output");
        var inBlock = objectMap.get(l.in().block());
        var m = l.arg() ? args : inputs;
        if (inBlock instanceof ApplicationBlock in) {
          m
            .computeIfAbsent(in, b -> new TreeMap<>())
            .computeIfAbsent(l.in().spot(), k -> new TreeMap<>())
            .put(l.in().index(), new Link(out, l.out().spot()));
        } else {
          throw new InvalidLinkException(l, "invalid type of input: " + inBlock);
        }
      });
    }

    private void resolveConstant(ApplicationConstant c) {
      try {
        var v = c.primitiveConstValue();
        beans.put(c, v == null ? constValue(c) : v);
      } catch (InvocationTargetException e) {
        throw new ConstantResolutionException(c, e.getTargetException());
      } catch (Throwable e) {
        throw new ConstantResolutionException(c, e);
      }
    }

    private Object constValue(ApplicationConstant c) throws Exception {
      var factoryClass = Class.forName(c.factory, true, currentThread().getContextClassLoader());
      var info = constantInfoCache.get(factoryClass);
      if (info == null) throw new NoConstantFoundException(c);
      return info.invoke(c.value);
    }

    private Object resolveBlock(ApplicationBlock b, int[] passed) {
      var id = b.id;
      var existingBean = beans.get(b);
      if (existingBean != null) return existingBean;
      var newPassed = tryAdd(passed, id);
      if (newPassed == passed) throw new CircularBlockReferenceException(passed, id);
      try {
        var m = method(b, newPassed);
        var argLinks = args.get(b);
        var bean = m.invoke(argLinks == null ? Map.of() : blockArgs(b, newPassed, argLinks, m));
        if (bean == null) throw new NullBlockResolutionException(b);
        else if (bean instanceof AutoCloseable c) runtimeApp.addCloseable(wrapCloseable(id, c));
        beans.put(b, bean);
        invokeInputs(b, bean, new int[]{id}); // do not allow to reference itself
        return bean;
      } catch (InvocationTargetException e) {
        throw new BlockResolutionException(b, e.getTargetException());
      } catch (Throwable e) {
        throw new BlockResolutionException(b, e);
      }
    }

    private Map<String, Object> blockArgs(ApplicationBlock b, int[] passed, TreeMap<String, TreeMap<Integer, Link>> ls, ResolvedMethod method) {
      return new TransformedMap<>(ls, (name, m) -> {
        var p = method.parameter(name);
        if (p == null) throw new NoSuchBlockArgumentException(b, name);
        try {
          return v(p, m, passed);
        } catch (Throwable e) {
          throw new BlockSetArgumentException(b, name, e);
        }
      });
    }

    private void invokeInputs(ApplicationBlock b, Object bean, int[] passed) {
      var links = this.inputs.get(b);
      if (links == null) return;
      var classInfo = classInfoCache.get(bean.getClass());
      links.forEach((spot, map) -> {
        try {
          var method = classInfo.input(spot);
          var param = method.getParameters()[0];
          method.invoke(bean, v(param, map, passed));
        } catch (InvocationTargetException e) {
          throw new BlockSetInputException(b, spot, e.getTargetException());
        } catch (Throwable e) {
          throw new BlockSetInputException(b, spot, e);
        }
      });
      if (bean instanceof Initializable i) {
        try {
          i.init();
        } catch (Throwable e) {
          throw new BlockInitializationException(b, e);
        }
      }
    }

    private void start() {
      beans.forEach((o, bean) -> {
        if (bean instanceof Startable s) {
          try {
            s.start();
          } catch (Throwable e) {
            throw new BlockStartException(o, e);
          }
        }
      });
    }

    private Object v(Parameter param, TreeMap<Integer, Link> map, int[] passed) {
      if (param.isVarArgs()) {
        var v = Array.newInstance(param.getType().getComponentType(), map.lastKey() + 1);
        map.forEach((i, l) -> Array.set(v, i, resolveOut(l.from(), l.out(), passed)));
        return v;
      } else {
        var l = map.lastEntry().getValue();
        return resolveOut(l.from(), l.out(), passed);
      }
    }

    private ResolvedMethod method(ApplicationBlock b, int[] passed) throws Exception {
      if (b.isDependent()) {
        var bean = switch (objectMap.get(b.parentBlockId())) {
          case null -> throw new NoSuchParentBlockException(b);
          case ApplicationBlock p -> resolveBlock(p, passed);
          case ApplicationConstant c -> beans.get(c);
        };
        var classInfo = classInfoCache.get(bean.getClass());
        return new ResolvedMethod(classInfo.factory(b.method), bean);
      } else {
        var type = Class.forName(b.factory, true, currentThread().getContextClassLoader());
        var classInfo = classInfoCache.get(type);
        return new ResolvedMethod(classInfo.staticFactory(b.method), type);
      }
    }

    private Object resolveOut(ResolvableObject out, String spot, int[] passed) {
      try {
        var bean = switch (out) {
          case ApplicationConstant c -> beans.get(c);
          case ApplicationBlock b -> resolveBlock(b, passed);
        };
        if ("*".equals(spot)) return bean;
        else return outValues
          .computeIfAbsent(out, k -> new TreeMap<>())
          .computeIfAbsent(spot, s -> {
            try {
              var classInfo = classInfoCache.get(bean.getClass());
              var output = classInfo.output(spot);
              return output.invoke(bean);
            } catch (InvocationTargetException e) {
              throw new OutputResolutionException(out, spot, e.getTargetException());
            } catch (ReflectiveOperationException e) {
              throw new OutputResolutionException(out, spot, e);
            }
          });
      } catch (Throwable e) {
        throw new OutputResolutionException(out, spot, e);
      }
    }
  }
}
