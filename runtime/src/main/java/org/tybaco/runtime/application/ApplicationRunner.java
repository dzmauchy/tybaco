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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.nio.charset.Charset;
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
      var runtimeApp = runtimeApp(activeApplication());
      getRuntime().addShutdownHook(new Thread(runtimeApp::close));
      runtimeApp.run();
    } finally {
      latch.countDown();
    }
  }

  private RuntimeApp runtimeApp(Application app) {
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
    private final HashMap<Conn, Conn> inputs;
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
        inputs.put(new Conn(inBlock, l.in().spot()), new Conn(outBlock, l.out().spot()));
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
        var params = resolvedMethod.method.getParameters();
        var resolvedParams = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
          var param = params[i];
          var in = new Conn(b, param.getName());
          var out = inputs.get(in);
          if (out == null) {
            resolvedParams[i] = defaultValue(param.getType());
          } else {
            var bean = resolveBlock(out.block, passed);
            resolvedParams[i] = resolveOut(out, bean);
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
        if (!method.canAccess(this)) continue;
        var spot = method.getName();
        var conn = new Conn(b, spot);
        var out = inputs.get(conn);
        if (out != null) {
          var outBean = resolveBlock(b, new BitSet());
          var v = resolveOut(out, outBean);
          try {
            method.invoke(bean, v);
          } catch (Throwable e) {
            throw new IllegalStateException("Unable to set %s on %d".formatted(spot, b.id()), e);
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
      var method = stream(Class.forName(b.factory(), true, classLoader).getMethods())
        .filter(m -> m.getName().equals(b.value()))
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException("Block %d factory method is not found".formatted(b.id())));
      var result = new ResolvedMethod(method, null);
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
        case "boolean" -> Boolean.parseBoolean(v);
        case "float" -> Float.parseFloat(v);
        case "double" -> Double.parseDouble(v);
        case "double[]" -> commaPattern.splitAsStream(v).map(String::trim).mapToDouble(Double::parseDouble).toArray();
        case "URL" -> new URI(v).toURL();
        case "URI" -> new URI(v);
        case "Charset" -> Charset.forName(v);
        case "Currency" -> Currency.getInstance(v);
        case "Locale" -> Locale.forLanguageTag(v);
        case "InetAddress" -> InetAddress.getByName(v);
        case "BigInteger" -> new BigInteger(v);
        case "BigDecimal" -> new BigDecimal(v);
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

    private Object defaultValue(Class<?> type) {
      if (type.isPrimitive()) {
        if (type == int.class) return 0;
        else if (type == long.class) return 0L;
        else if (type == short.class) return (short) 0;
        else if (type == char.class) return (char) 0;
        else if (type == byte.class) return (byte) 0;
        else if (type == float.class) return 0f;
        else if (type == double.class) return 0d;
        else if (type == boolean.class) return Boolean.FALSE;
      }
      return null;
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

  private record RuntimeApp(LinkedList<Ref<Runnable>> tasks, LinkedList<Ref<AutoCloseable>> closeables) {

    private void run() {
      for (var it = tasks.listIterator(0); it.hasNext(); ) {
        var task = it.next();
        try {
          task.ref.run();
        } catch (Throwable e) {
          throw new IllegalStateException("Unable to run %d".formatted(task.blockId), e);
        } finally {
          it.remove();
        }
      }
    }

    private void close() {
      var exception = new IllegalStateException("Close runtime exception");
      for (var it = closeables.listIterator(closeables.size()); it.hasPrevious(); ) {
        var closeable = it.previous();
        try {
          closeable.ref.close();
        } catch (Throwable e) {
          exception.addSuppressed(new IllegalStateException("Unable to close %d".formatted(closeable.blockId), e));
        } finally {
          it.remove();
        }
      }
      if (exception.getSuppressed().length > 0) {
        throw exception;
      }
    }
  }

  private record Ref<T>(T ref, int blockId) {}
}
