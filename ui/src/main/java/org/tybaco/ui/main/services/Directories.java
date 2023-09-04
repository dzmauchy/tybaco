package org.tybaco.ui.main.services;

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

import org.tybaco.ui.lib.context.EagerComponent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@EagerComponent
public final class Directories {

  public final Path userHome;
  public final Path userDir;
  public final Path appRoot;
  public final Path webRoot;

  public Directories() throws IOException {
    userHome = Path.of(System.getProperty("user.home"));
    userDir = Path.of(System.getProperty("user.dir"));
    appRoot = userHome.resolve(".tybaco");
    webRoot = appRoot.resolve("web");
    Files.createDirectories(webRoot);
  }
}
