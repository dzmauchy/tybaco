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

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ArtifactClassPath implements Closeable {

  private final Path directory;
  private final URLClassLoader classLoader;

  public ArtifactClassPath(Path directory, String classPathName) {
    this.directory = directory;
    this.classLoader = classLoader(classPathName);
  }

  public URLClassLoader getClassLoader() {
    return classLoader;
  }

  private URLClassLoader classLoader(String name) {
    try (var ds = Files.walk(directory)) {
      var uris = ds.filter(f -> f.getFileName().toString().endsWith(".jar")).map(Path::toUri).toArray(URI[]::new);
      var urls = new URL[uris.length];
      for (int i = 0; i < urls.length; i++) {
        urls[i] = uris[i].toURL();
      }
      return new URLClassLoader(name, urls, ClassLoader.getPlatformClassLoader());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void close() throws IOException {
    var closeable = new PathCloseable(directory);
    try (closeable) {
      classLoader.close();
    }
  }
}
