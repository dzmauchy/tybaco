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
import org.tybaco.runtime.application.tasks.LogConfigurer;
import org.tybaco.runtime.application.tasks.PluginLoader;

public final class Main {

  public static void main(String... args)  {
    var context = new ApplicationContext();
    execute("initLogging", context, new LogConfigurer());
    execute("loadPlugins", context, new PluginLoader());
    execute("loadApplication", context, new ApplicationLoader(args));
    execute("runApplication", context, new ApplicationRunner());
  }

  private static void execute(String step, ApplicationContext context, ApplicationTask task) {
    try {
      task.run(context);
    } catch (Throwable e) {
      throw new BootstrapException(step, e);
    }
  }
}
