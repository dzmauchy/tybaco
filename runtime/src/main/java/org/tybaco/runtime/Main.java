package org.tybaco.runtime;

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

import org.tybaco.runtime.application.tasks.*;
import org.tybaco.runtime.exception.BootstrapException;

import java.util.concurrent.CountDownLatch;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.locks.LockSupport.parkNanos;
import static org.tybaco.runtime.util.Settings.booleanSetting;
import static org.tybaco.runtime.util.Settings.longSetting;

public final class Main {

  public static void main(String... args) {
    var latch = new CountDownLatch(1);
    try {
      var watchdog = new Thread(() -> watch(latch), "application-watchdog");
      watchdog.setDaemon(true);
      watchdog.start();
      var context = requireNonNull(run(args), "Application initialization error");
      Runtime.getRuntime().addShutdownHook(new Thread(context));
    } finally {
      latch.countDown();
    }
  }

  public static Runnable run(String... args) {
    var context = new ApplicationContext();
    execute("initLogging", context, new LogConfigurer());
    execute("loadPlugins", context, new PluginLoader());
    execute("loadApplication", context, new ApplicationLoader(args));
    execute("runApplication", context, new ApplicationRunner());
    return context.closeable;
  }

  private static void execute(String step, ApplicationContext context, ApplicationTask task) {
    try {
      task.run(context);
    } catch (Throwable e) {
      throw new BootstrapException(step, e);
    }
  }

  private static void watch(CountDownLatch latch) {
    try {
      latch.await();
      if (!booleanSetting("TYBACO_EXIT_ENABLED").orElse(false)) {
        return;
      }
      parkNanos(longSetting("TYBACO_EXIT_WAIT_TIMEOUT").orElse(1L) * 1_000_000_000L);
      System.exit(0);
    } catch (Throwable e) {
      e.printStackTrace(System.err);
    }
  }
}
