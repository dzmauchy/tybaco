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
import org.tybaco.runtime.basic.CanBeStarted;
import org.tybaco.runtime.reflect.ClassInfoCache;
import org.tybaco.runtime.reflect.ConstantInfoCache;
import org.tybaco.runtime.util.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;

public class ApplicationRunner implements ApplicationTask {

  @Override
  public void run(ApplicationContext context) {
    requireNonNull(context.application, "Application is not loaded");
    var runtimeApp = requireNonNull(runtimeApp(context.application));
    context.closeable = runtimeApp::close;
    runtimeApp.run();
  }

  RuntimeApp runtimeApp(Application app) {
    var resolver = new ApplicationResolver(app);
    var closeables = resolver.closeables;
    var tasks = new LinkedList<Ref<CanBeStarted>>();
    var runtime = new RuntimeApp(tasks, closeables);
    try {
      for (var constant : app.constants()) {
        resolver.resolveConstant(constant);
      }
      for (var block : app.blocks()) {
        var bean = resolver.resolveBlock(block, new BitSet());
        if (bean instanceof CanBeStarted s) {
          tasks.addLast(new Ref<>(s, block.id()));
        }
      }
      for (var block : app.blocks()) {
        resolver.invokeInputs(block);
      }
      return runtime;
    } catch (Throwable e) {
      try {
        runtime.close();
      } catch (Throwable x) {
        e.addSuppressed(x);
      }
      throw e;
    }
  }

  private static final class ApplicationResolver {

    private final ClassInfoCache classInfoCache = new ClassInfoCache();
    private final ConstantInfoCache constantInfoCache = new ConstantInfoCache();
    private final IdentityHashMap<ResolvableObject, Object> beans;
    private final HashMap<Conn, Conns> args;
    private final HashMap<Conn, Conns> inputs;
    private final HashMap<Conn, Object> outValues;
    private final ResolvableObjectMap objectMap;
    private final LinkedList<Ref<AutoCloseable>> closeables = new LinkedList<>();

    private ApplicationResolver(Application app) {
      this.beans = new IdentityHashMap<>(app.blocks().size());
      this.args = new HashMap<>(app.links().size());
      this.inputs = new HashMap<>(app.links().size());
      this.outValues = new HashMap<>(app.links().size());
      this.objectMap = new ResolvableObjectMap(app.maxInternalId() + 1);

      app.blocks().forEach(b -> objectMap.put(b.id(), b));
      app.constants().forEach(c -> objectMap.put(c.id(), c));

      app.links().forEach(l -> {
        var outBlock = requireNonNull(objectMap.get(l.out().block()), () -> "Object %d doesn't exist".formatted(l.out().block()));
        var inBlock = requireNonNull(objectMap.get(l.in().block()), () -> "Object %d doesn't exist".formatted(l.in().block()));
        var m = l.arg() ? args : inputs;
        m.computeIfAbsent(new Conn(inBlock, l.in().spot()), k -> new Conns()).add(l.in().index(), new Conn(outBlock, l.out().spot()));
      });
    }

    private void resolveConstant(ApplicationConstant c) {
      try {
        var v = c.primitiveConstValue();
        beans.put(c, v == null ? constValue(c) : v);
      } catch (Throwable e) {
        throw new IllegalStateException("Unable to create " + c);
      }
    }

    private Object constValue(ApplicationConstant c) throws Exception {
      var factoryClass = Class.forName(c.factory(), true, currentThread().getContextClassLoader());
      var info = requireNonNull(constantInfoCache.get(factoryClass), () -> "No constant found in " + factoryClass);
      return info.invoke(c.value());
    }

    private Object resolveBlock(ApplicationBlock b, BitSet passed) {
      if (beans.containsKey(b)) return beans.get(b);
      if (passed.get(b.id())) throw new IllegalStateException("Circular reference of blocks: %s".formatted(passed));
      passed.set(b.id());
      try {
        var resolvedMethod = method(b, passed);
        var params = resolvedMethod.method.getParameters();
        var resolvedParams = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
          var param = params[i];
          var in = new Conn(b, param.getName());
          var out = args.get(in);
          if (out == null) {
            resolvedParams[i] = defaultValue(param);
          } else if (param.isVarArgs()) {
            var array = Array.newInstance(param.getType().getComponentType(), out.len());
            out.forEach((index, conn) -> {
              var bean = beans.containsKey(conn.block()) ? beans.get(conn.block()) : resolveBlock((ApplicationBlock) conn.block(), passed);
              Array.set(array, index, resolveOut(conn, bean));
            });
            resolvedParams[i] = array;
          } else {
            var conn = out.get(0);
            var bean = beans.containsKey(conn.block()) ? beans.get(conn.block()) : resolveBlock((ApplicationBlock) conn.block(), passed);
            resolvedParams[i] = resolveOut(conn, bean);
          }
        }
        var bean = resolvedMethod.method.invoke(resolvedMethod.bean, resolvedParams);
        if (bean instanceof AutoCloseable c) {
          closeables.addLast(new Ref<>(c, b.id()));
        }
        beans.put(b, bean);
        return bean;
      } catch (Throwable e) {
        throw new IllegalStateException("Unable to resolve %d".formatted(b.id()), e);
      }
    }

    private void invokeInputs(ApplicationBlock b) {
      var bean = beans.get(b);
      for (var method : bean.getClass().getMethods()) {
        if (method.getParameterCount() != 1) continue;
        if (Modifier.isStatic(method.getModifiers())) continue;
        var conn = new Conn(b, "+" + method.getName());
        var out = args.get(conn);
        if (out != null) {
          var parameter = method.getParameters()[0];
          final Object arg;
          if (parameter.isVarArgs()) {
            arg = Array.newInstance(parameter.getType().getComponentType(), out.len());
            out.forEach((i, c) -> {
              var outBean = beans.get(c.block());
              Array.set(arg, i, resolveOut(c, outBean));
            });
          } else {
            var outConn = out.get(0);
            var outBean = beans.get(outConn.block());
            arg = resolveOut(outConn, outBean);
          }
          try {
            method.invoke(bean, arg);
          } catch (Throwable e) {
            throw new IllegalStateException("Unable to set %s on %d".formatted(method.getName(), b.id()), e);
          }
        }
      }
    }

    private ResolvedMethod method(ApplicationBlock b, BitSet passed) throws Exception {
      if (b.isDependent()) {
        var parentBlockId = b.parentBlockId();
        var parentBlock = requireNonNull(objectMap.get(parentBlockId), () -> "Block %d doesn't exist".formatted(parentBlockId));
        var bean = beans.containsKey(parentBlock) ? beans.get(parentBlock) : resolveBlock((ApplicationBlock) parentBlock, passed);
        var method = b.resolveFactoryMethod(bean);
        return new ResolvedMethod(method, bean);
      }
      var type = Class.forName(b.factory(), true, currentThread().getContextClassLoader());
      var method = b.resolveFactoryMethod(type);
      return new ResolvedMethod(method, type);
    }

    private record ResolvedMethod(Method method, Object bean) {}

    private static Object defaultValue(Parameter parameter) {
      if (parameter.getType().isPrimitive()) {
        return Array.get(Array.newInstance(parameter.getType(), 1), 0);
      } else if (parameter.isVarArgs()) {
        return Array.newInstance(parameter.getType().getComponentType(), 0);
      } else {
        return null;
      }
    }

    private Object resolveOut(Conn out, Object bean) {
      if ("*".equals(out.spot())) return bean;
      else if (outValues.containsKey(out)) return outValues.get(out);
      else {
        try {
          var method = out.getClass().getMethod(out.spot());
          var value = method.invoke(bean);
          outValues.put(out, value);
          return value;
        } catch (Throwable e) {
          throw new IllegalStateException("Block %d: error on resolving output %s".formatted(out.block().id(), out.spot()), e);
        }
      }
    }
  }

  record RuntimeApp(LinkedList<Ref<CanBeStarted>> tasks, LinkedList<Ref<AutoCloseable>> closeables) implements AutoCloseable {

    void run() {
      while (true) {
        var ref = tasks.pollFirst();
        if (ref == null) break;
        try {
          ref.ref.start();
        } catch (Throwable e) {
          try {
            close();
          } catch (Throwable x) {
            e.addSuppressed(x);
          }
          throw new IllegalStateException("Unable to run %d".formatted(ref.id), e);
        }
      }
    }

    @Override
    public void close() {
      var exception = new IllegalStateException("Application close error");
      while (true) {
        var closeable = closeables.pollLast();
        if (closeable == null) break;
        try {
          closeable.ref.close();
        } catch (Throwable e) {
          exception.addSuppressed(new IllegalStateException("Unable to close %d".formatted(closeable.id), e));
        }
      }
      if (exception.getSuppressed().length > 0) {
        throw exception;
      }
    }
  }

  private record Ref<T>(T ref, int id) {}
}
