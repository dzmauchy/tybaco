package org.tybloco.ui.child.project;

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

import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.tybloco.ui.main.services.Directories;
import org.tybloco.ui.model.Project;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class ProjectWebUserData implements Closeable {

  public final Path directory;

  public ProjectWebUserData(Directories directories, Project project) throws IOException {
    directory = Files.createTempDirectory(directories.webRoot, project.id + "_");
  }

  @Override
  public void close() throws IOException {
    FileSystemUtils.deleteRecursively(directory);
  }
}
