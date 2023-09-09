package org.tybaco.runtime.plugins;

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

import java.util.Comparator;
import java.util.ServiceLoader;

public final class PluginLoader implements Runnable {

  @Override
  public void run() {
    var loader = ServiceLoader.load(Plugin.class);
    try (var pluginProviderStream = loader.stream()) {
      pluginProviderStream
        .map(provider -> {
          try {
            return provider.get();
          } catch (Throwable e) {
            throw new PluginException(provider.type(), e);
          }
        })
        .sorted(Comparator.comparingInt(Plugin::getPriority))
        .forEachOrdered(plugin -> {
          try {
            plugin.run();
          } catch (Throwable e) {
            throw new PluginException(plugin.getClass(), e);
          }
        });
    } finally {
      loader.reload();
    }
  }
}
