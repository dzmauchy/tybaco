package org.tybaco.runtime.application;

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

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;

import static java.lang.Long.parseLong;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.*;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.concurrent.locks.LockSupport.parkNanos;
import static org.tybaco.runtime.application.Application.activeApplication;

public class ApplicationRunner implements Runnable {

  @Override
  public void run() {
    var latch = new CountDownLatch(1);
    var watchdog = new Thread(() -> watch(latch), "application-watchdog");
    watchdog.setDaemon(true);
    watchdog.start();
    try {
      //noinspection resource
      var runtimeApp = runtimeApp(activeApplication());
      getRuntime().addShutdownHook(new Thread(runtimeApp::close));
      runtimeApp.run();
    } finally {
      latch.countDown();
    }
  }

  RuntimeApp runtimeApp(Application app) {
    var resolver = new ApplicationResolver(app);
    var closeables = resolver.closeables;
    var tasks = new LinkedList<Ref<Runnable>>();
    var runtime = new RuntimeApp(tasks, closeables);
    try {
      for (var constant : app.constants()) {
        resolver.resolveConstant(constant);
      }
      for (var block : app.blocks()) {
        var bean = resolver.resolveBlock(block, new BitSet());
        if (bean instanceof Thread t) {
          tasks.add(new Ref<>(t::start, block.id()));
        } else if (bean instanceof Runnable r) {
          tasks.add(new Ref<>(r, block.id()));
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
    } finally {
      Application.CURRENT_APPLICATION.remove();
    }
  }

  private static void watch(CountDownLatch latch) {
    try {
      latch.await();
      var exitEnabled = "true".equals(getProperty("tybaco.app.exit.enabled")) || "true".equals(getenv("TYBACO_APP_EXIT_ENABLED"));
      if (!exitEnabled) {
        return;
      }
      var waitTimeout = parseLong(getProperty("tybaco.app.exit.wait.timeout", requireNonNullElse(getenv("TYBACO_APP_EXIT_WAIT_TIMEOUT"), "1")));
      parkNanos(waitTimeout * 1_000_000_000L);
      System.exit(0);
    } catch (Throwable e) {
      e.printStackTrace(System.err);
    }
  }

  private static final class ApplicationResolver {

    private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private final IdentityHashMap<ResolvableObject, Object> beans;
    private final HashMap<Conn, TreeMap<Integer, Conn>> inputs;
    private final HashMap<Conn, Object> outValues;
    private final IntMap<ResolvableObject> objectMap;
    private final LinkedList<Ref<AutoCloseable>> closeables = new LinkedList<>();

    private ApplicationResolver(Application app) {
      this.beans = new IdentityHashMap<>(app.blocks().size());
      this.inputs = new HashMap<>(app.links().size());
      this.outValues = new HashMap<>(app.links().size());
      this.objectMap = new IntMap<>(app.maxInternalId() + 1);

      app.blocks().forEach(b -> objectMap.put(b.id(), b));
      app.constants().forEach(c -> objectMap.put(c.id(), c));

      app.links().forEach(l -> {
        var outBlock = requireNonNull(objectMap.get(l.out().block()), () -> "Object %d doesn't exist".formatted(l.out().block()));
        var inBlock = requireNonNull(objectMap.get(l.in().block()), () -> "Object %d doesn't exist".formatted(l.in().block()));
        inputs.computeIfAbsent(new Conn(inBlock, l.in().spot()), k -> new TreeMap<>()).put(l.in().index(), new Conn(outBlock, l.out().spot()));
      });
    }

    private void resolveConstant(ApplicationConstant c) {
      try {
        var v = primitiveConstValue(c);
        beans.put(c, v == null ? constValue(c) : v);
      } catch (Throwable e) {
        throw new IllegalStateException("Unable to create " + c);
      }
    }

    private Object constValue(ApplicationConstant c) throws Exception {
      var factoryClass = Class.forName(c.factory(), true, classLoader);
      for (var method : factoryClass.getMethods()) {
        if (ApplicationConstant.isFactoryExecutable(method)) {
          return method.invoke(null, c.value());
        }
      }
      for (var constructor : factoryClass.getConstructors()) {
        if (ApplicationConstant.isFactoryExecutable(constructor)) {
          return constructor.newInstance(c.value());
        }
      }
      throw new NoSuchElementException("No value methods found on " + factoryClass);
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
          var out = inputs.get(in);
          if (out == null) {
            resolvedParams[i] = defaultValue(param.getType(), param.isVarArgs());
          } else if (param.isVarArgs()) {
            var array = Array.newInstance(param.getType().getComponentType(), out.lastKey() + 1);
            out.forEach((index, o) -> {
              var bean = beans.containsKey(o.block) ? beans.get(o.block) : resolveBlock((ApplicationBlock) o.block, passed);
              Array.set(array, index, resolveOut(o, bean));
            });
            resolvedParams[i] = array;
          } else {
            var conn = out.firstEntry().getValue();
            var bean = beans.containsKey(conn.block) ? beans.get(conn.block) : resolveBlock((ApplicationBlock) conn.block(), passed);
            resolvedParams[i] = resolveOut(conn, bean);
          }
        }
        var bean = resolvedMethod.method.invoke(resolvedMethod.bean, resolvedParams);
        if (bean instanceof AutoCloseable c) {
          closeables.addLast(new Ref<>(c, b.id()));
        } else if (bean instanceof Timer t) {
          closeables.addLast(new Ref<>(t::cancel, b.id()));
        } else if (bean instanceof TimerTask t) {
          closeables.addLast(new Ref<>(t::cancel, b.id()));
        } else if (bean instanceof ScheduledFuture<?> f) {
          closeables.addLast(new Ref<>(() -> f.cancel(false), b.id()));
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
        var out = inputs.get(conn);
        if (out != null) {
          var parameter = method.getParameters()[0];
          final Object arg;
          if (parameter.isVarArgs()) {
            arg = Array.newInstance(parameter.getType().getComponentType(), out.lastKey() + 1);
            out.forEach((i, o) -> {
              var outBean = beans.get(o.block);
              Array.set(arg, i, resolveOut(o, outBean));
            });
          } else {
            var outConn = out.firstEntry().getValue();
            var outBean = beans.get(outConn.block);
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
      var type = Class.forName(b.factory(), true, classLoader);
      var method = b.resolveFactoryMethod(type);
      return new ResolvedMethod(method, type);
    }

    private record Conn(ResolvableObject block, String spot) {

      @Override
      public boolean equals(Object obj) {
        return obj instanceof Conn c && block == c.block && spot.equals(c.spot);
      }

      @Override
      public int hashCode() {
        return identityHashCode(block) ^ spot.hashCode();
      }

      @Override
      public String toString() {
        return block.id() + "(" + spot + ")";
      }
    }

    private record ResolvedMethod(Method method, Object bean) {}

    private Object defaultValue(Class<?> type, boolean varargs) {
      if (type.isPrimitive()) {
        if (type == int.class) return 0;
        else if (type == long.class) return 0L;
        else if (type == short.class) return (short) 0;
        else if (type == char.class) return (char) 0;
        else if (type == byte.class) return (byte) 0;
        else if (type == float.class) return 0f;
        else if (type == double.class) return 0d;
        else if (type == boolean.class) return Boolean.FALSE;
        else return null;
      } else if (varargs) {
        return Array.newInstance(type.getComponentType(), 0);
      } else {
        return null;
      }
    }

    private Object resolveOut(Conn out, Object bean) {
      if ("*".equals(out.spot)) return bean;
      else if (outValues.containsKey(out)) return outValues.get(out);
      else {
        try {
          var method = out.getClass().getMethod(out.spot);
          var value = method.invoke(bean);
          outValues.put(out, value);
          return value;
        } catch (Throwable e) {
          throw new IllegalStateException("Block %d: error on resolving output %s".formatted(out.block.id(), out.spot), e);
        }
      }
    }
  }

  record RuntimeApp(LinkedList<Ref<Runnable>> tasks, LinkedList<Ref<AutoCloseable>> closeables) implements AutoCloseable {

    void run() {
      for (var it = tasks.listIterator(0); it.hasNext(); ) {
        var task = it.next();
        try {
          task.ref.run();
        } catch (Throwable e) {
          throw new IllegalStateException("Unable to run %d".formatted(task.id), e);
        } finally {
          it.remove();
        }
      }
    }

    @Override
    public void close() {
      var exception = new IllegalStateException("Close application runtime error");
      for (var it = closeables.listIterator(closeables.size()); it.hasPrevious(); ) {
        var closeable = it.previous();
        try {
          closeable.ref.close();
        } catch (Throwable e) {
          exception.addSuppressed(new IllegalStateException("Unable to close %d".formatted(closeable.id), e));
        } finally {
          it.remove();
        }
      }
      if (exception.getSuppressed().length > 0) {
        throw exception;
      }
    }
  }

  private static Object primitiveConstValue(ApplicationConstant b) {
    var v = b.value();
    return switch (b.factory()) {
      case "int" -> Integer.parseInt(v);
      case "long" -> parseLong(v);
      case "short" -> Short.parseShort(v);
      case "byte" -> Byte.parseByte(v);
      case "char" -> v.charAt(0);
      case "boolean" -> Boolean.parseBoolean(v);
      case "float" -> Float.parseFloat(v);
      case "double" -> Double.parseDouble(v);
      default -> null;
    };
  }

  private record Ref<T>(T ref, int id) {}

  @SuppressWarnings("unchecked")
  private static final class IntMap<T> {

    private static final int BUCKET_SIZE = 64;

    private Object[][] buckets;

    private IntMap(int size) {
      var fixedSize = (size % BUCKET_SIZE) == 0 ? size / BUCKET_SIZE : size / BUCKET_SIZE + 1;
      buckets = new Object[fixedSize][];
    }

    private void put(int key, T value) {
      var bucketIndex = key / BUCKET_SIZE;
      if (buckets.length <= bucketIndex) buckets = Arrays.copyOf(buckets, bucketIndex + 1, Object[][].class);
      var bucket = buckets[bucketIndex];
      if (bucket == null) buckets[bucketIndex] = bucket = new Object[BUCKET_SIZE];
      bucket[key % BUCKET_SIZE] = value;
    }

    private T get(int key) {
      var bucketIndex = key / BUCKET_SIZE;
      if (bucketIndex >= buckets.length) return null;
      return (T) buckets[bucketIndex][key % BUCKET_SIZE];
    }

    @Override
    public String toString() {
      var map = new TreeMap<Integer, Object>();
      int i = 0;
      for (var bucket : buckets) {
        if (bucket == null) {
          i += BUCKET_SIZE;
        } else {
          for (var v : bucket) {
            if (v != null) {
              map.put(i++, v);
            } else {
              i++;
            }
          }
        }
      }
      return map.toString();
    }
  }
}
