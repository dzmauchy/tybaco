package org.tybloco.runtime.util;

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

import java.io.*;
import java.net.URL;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public interface IO {

  static Properties loadProperties(URL url) {
    try (var is = url.openStream(); var r = new InputStreamReader(is, UTF_8)) {
      var props = new Properties();
      props.load(r);
      return props;
    } catch (IOException e) {
      throw new UncheckedIOException("Unable to load " + url, e);
    }
  }

  static Properties loadProperties(String resource) {
    var classLoader = Thread.currentThread().getContextClassLoader();
    var url = requireNonNull(classLoader.getResource(resource), () -> "No such resource: " + resource);
    return loadProperties(url);
  }
}
