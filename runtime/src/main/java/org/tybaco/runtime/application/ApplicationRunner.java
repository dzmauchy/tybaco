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
import java.net.URI;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

import static java.lang.Long.parseLong;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.*;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.concurrent.locks.LockSupport.parkNanos;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;
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

    private final Pattern commaPattern = Pattern.compile(",");
    private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private final IdentityHashMap<Block, ResolvedMethod> methods;
    private final IdentityHashMap<Block, Object> beans;
    private final HashMap<Conn, TreeMap<Integer, Conn>> inputs;
    private final HashMap<Conn, Object> outValues;
    private final Map<Integer, Block> blockMap;
    private final LinkedList<Ref<AutoCloseable>> closeables = new LinkedList<>();

    private ApplicationResolver(Application application) {
      this.methods = new IdentityHashMap<>(application.blocks().size());
      this.beans = new IdentityHashMap<>(application.blocks().size());
      this.inputs = new HashMap<>(application.links().size());
      this.outValues = new HashMap<>(application.links().size());
      this.blockMap = application.blocks().stream().collect(toUnmodifiableMap(Block::id, identity()));

      application.links().forEach(l -> {
        var outBlock = requireNonNull(blockMap.get(l.out().block()), () -> "Block %d doesn't exist".formatted(l.out().block()));
        var inBlock = requireNonNull(blockMap.get(l.in().block()), () -> "Block %d doesn't exist".formatted(l.in().block()));
        inputs.computeIfAbsent(new Conn(inBlock, l.in().spot()), k -> new TreeMap<>()).put(l.in().index(), new Conn(outBlock, l.out().spot()));
      });
    }

    private Object resolveBlock(Block b, BitSet passed) {
      {
        var bean = beans.get(b);
        if (bean != null) {
          return bean;
        }
      }
      if (passed.get(b.id())) {
        throw new IllegalStateException("Circular reference of blocks: %s".formatted(passed));
      }
      passed.set(b.id());
      try {
        {
          var constValue = resolveConstant(b);
          if (constValue != null) {
            beans.put(b, constValue);
            return constValue;
          }
        }
        var resolvedMethod = method(b, passed);
        if (resolvedMethod.method == null) {
          if (resolvedMethod.bean instanceof Class<?> c) {
            var constValue = constValue(c, b.value());
            beans.put(b, constValue);
            return constValue;
          } else {
            throw new IllegalStateException("Unresolved method %s".formatted(b.value()));
          }
        }
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
              var bean = resolveBlock(o.block, passed);
              Array.set(array, index, resolveOut(o, bean));
            });
            resolvedParams[i] = array;
          } else {
            var conn = out.firstEntry().getValue();
            var bean = resolveBlock(conn.block(), passed);
            resolvedParams[i] = resolveOut(conn, bean);
          }
        }
        var bean = resolvedMethod.method.invoke(resolvedMethod.bean, resolvedParams);
        if (bean instanceof AutoCloseable c) {
          closeables.addLast(new Ref<>(c, b.id()));
        } else if (bean instanceof Timer t) {
          closeables.addLast(new Ref<>(t::cancel, b.id()));
        }
        beans.put(b, bean);
        return bean;
      } catch (Throwable e) {
        throw new IllegalStateException("Unable to resolve %d".formatted(b.id()), e);
      }
    }

    private void invokeInputs(Block b) {
      var bean = beans.get(b);
      for (var method : bean.getClass().getMethods()) {
        if (method.getParameterCount() != 1) continue;
        if (Modifier.isStatic(method.getModifiers())) continue;
        if (method.getReturnType() != void.class) continue;
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

    private ResolvedMethod method(Block b, BitSet passed) throws Exception {
      {
        var m = methods.get(b);
        if (m != null) {
          return m;
        }
      }
      if (b.isDependent()) {
        var parentBlockId = b.parentBlockId();
        var parentBlock = requireNonNull(blockMap.get(parentBlockId), () -> "Block %d doesn't exist".formatted(parentBlockId));
        var bean = resolveBlock(parentBlock, passed);
        var method = resolveFactoryMethod(parentBlock.id(), b.value(), bean);
        var result = new ResolvedMethod(method, bean);
        methods.put(b, result);
        return result;
      }
      var type = Class.forName(b.factory(), true, classLoader);
      var method = stream(type.getMethods())
        .filter(m -> m.getName().equals(b.value()))
        .findFirst()
        .orElse(null);
      var result = new ResolvedMethod(method, type);
      methods.put(b, result);
      return result;
    }

    private Object resolveConstant(Block b) throws Exception {
      var v = b.value();
      return switch (b.factory()) {
        case "int" -> Integer.parseInt(v);
        case "int[]" -> commaPattern.splitAsStream(v).map(String::trim).mapToInt(Integer::parseInt).toArray();
        case "long" -> parseLong(v);
        case "long[]" -> commaPattern.splitAsStream(v).map(String::trim).mapToLong(Long::parseLong).toArray();
        case "short" -> Short.parseShort(v);
        case "byte" -> Byte.parseByte(v);
        case "char" -> v.charAt(0);
        case "codePoint" -> v.chars().findFirst().orElseThrow();
        case "boolean" -> Boolean.parseBoolean(v);
        case "float" -> Float.parseFloat(v);
        case "double" -> Double.parseDouble(v);
        case "double[]" -> commaPattern.splitAsStream(v).map(String::trim).mapToDouble(Double::parseDouble).toArray();
        case "URL" -> new URI(v).toURL();
        case "URI" -> new URI(v);
        case "Locale" -> Locale.forLanguageTag(v);
        case "String" -> v;
        case "Random" -> new Random(v.isEmpty() ? 0L : parseLong(v));
        case "Pattern" -> Pattern.compile(v);
        case "stringBytes" -> v.getBytes(StandardCharsets.UTF_8);
        case "stringChars" -> v.toCharArray();
        case "stringCodePoints" -> v.chars().toArray();
        case "DateTimeFormatter" -> DateTimeFormatter.ofPattern(v, Locale.getDefault());
        case "TimeZone" -> TimeZone.getTimeZone(v);
        case "ByteOrder" -> switch (v) {
          case "BE" -> ByteOrder.BIG_ENDIAN;
          case "LE" -> ByteOrder.LITTLE_ENDIAN;
          default -> ByteOrder.nativeOrder();
        };
        default -> null;
      };
    }

    private record Conn(Block block, String spot) {

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
      if ("*".equals(out.spot)) {
        return bean;
      } else {
        {
          var v = outValues.get(out);
          if (v != null) {
            return v;
          }
        }
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

    private Method resolveFactoryMethod(int blockId, String factory, Object bean) {
      try {
        return stream(bean.getClass().getMethods())
          .filter(m -> !Modifier.isStatic(m.getModifiers()))
          .filter(m -> m.getName().equals(factory))
          .findFirst()
          .orElseThrow(() -> new NoSuchElementException(factory));
      } catch (Throwable e) {
        throw new IllegalStateException("Block %d: error on resolving factory %s".formatted(blockId, factory));
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

  private static Object constValue(Class<?> type, String value) throws Exception {
    for (var method : type.getMethods()) {
      if (!Modifier.isStatic(method.getModifiers())) continue;
      if (method.getParameterCount() != 1) continue;
      if (method.getParameterTypes()[0] != String.class) continue;
      switch (method.getName()) {
        case "valueOf", "parse", "of", "getInstance", "instance", "instanceOf", "getByName", "byName", "forName" -> {
          return method.invoke(type, value);
        }
      }
    }
    for (var constructor : type.getConstructors()) {
      if (constructor.getParameterCount() != 1) continue;
      if (constructor.getParameterTypes()[0] != String.class) continue;
      return constructor.newInstance(value);
    }
    throw new NoSuchElementException("No value methods found on " + type);
  }

  private record Ref<T>(T ref, int id) {}
}
