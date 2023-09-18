package org.tybaco.ui.lib.repo;

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

import org.tybaco.io.PathCloseable;
import org.tybaco.ui.lib.logging.Logging;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.logging.Level.INFO;
import static org.tybaco.ui.lib.logging.Logging.LOG;

public class ArtifactClassPath implements Closeable {

  final Path directory;
  public final URLClassLoader classLoader;

  public ArtifactClassPath(Path directory, String classPathName) {
    this.directory = directory;
    this.classLoader = classLoader(classPathName);
  }

  private URLClassLoader classLoader(String name) {
    if (directory == null) {
      return new URLClassLoader(new URL[0], ClassLoader.getPlatformClassLoader());
    }
    try (var ds = Files.walk(directory)) {
      var urls = ds
        .filter(f -> f.getFileName().toString().endsWith(".jar"))
        .map(Path::toUri)
        .peek(uri -> LOG.log(INFO, "Adding {0} to the classpath", uri))
        .map(uri -> {
          try {
            return uri.toURL();
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        })
        .toArray(URL[]::new);
      return new URLClassLoader(name, urls, ClassLoader.getPlatformClassLoader());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void close() throws IOException {
    try (var closeable = new PathCloseable(directory)) {
      LOG.log(INFO, "Deleting {0}", closeable.path());
      classLoader.close();
    }
    if (Files.notExists(directory)) {
      LOG.log(INFO, "{0} deleted successfully", directory);
    }
  }
}
