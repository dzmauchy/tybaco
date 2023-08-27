package org.tybaco.ui.main.projects;

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
import org.tybaco.ui.main.services.Directories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@EagerComponent
public class Projects {

  public final Path projectsDirectory;

  public Projects(Directories directories) throws IOException {
    projectsDirectory = directories.appRoot.resolve("projects");
    Files.createDirectories(projectsDirectory);
  }
}
