package org.tybaco.ui.main.project;

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

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.springframework.stereotype.Component;
import org.tybaco.editors.text.Texts;
import org.tybaco.ui.main.MainStage;
import org.tybaco.ui.model.Project;
import org.tybaco.xml.Xml;

import java.io.File;
import java.util.List;
import java.util.prefs.Preferences;

import static org.tybaco.logging.Log.info;
import static org.tybaco.logging.Log.warn;

@Component
public final class ProjectLoadDialog {

  private static final Preferences PREFERENCES = Preferences.userNodeForPackage(ProjectLoadDialog.class);

  private final Projects projects;

  public ProjectLoadDialog(Projects projects) {
    this.projects = projects;
  }

  public List<File> showAndWait() {
    var dir = new File(PREFERENCES.get("loadDir", System.getProperty("user.dir")));
    var chooser = new FileChooser();
    chooser.titleProperty().bind(Texts.text("Load project"));
    chooser.setInitialDirectory(dir.isDirectory() ? dir : new File(System.getProperty("user.dir")));
    chooser.getExtensionFilters().add(new ExtensionFilter("*.xml", "*.xml"));
    return chooser.showOpenMultipleDialog(MainStage.mainStage());
  }

  public void load() {
    showAndWait().forEach(file -> {
      try {
        var project = Xml.loadFrom(file, null, Project::new);
        info(getClass(), "Project {0} was loaded successfully from {1}", project.id, file);
        if (projects.projects.stream().noneMatch(p -> p.id.equals(project.id))) {
          projects.projects.add(project);
          info(getClass(), "Project {0} was added successfully", project.id);
        } else {
          info(getClass(), "Project {0} was already opened before", project.id);
        }
      } catch (Throwable e) {
        warn(getClass(), "Unable to load {0}", e, file);
      }
    });
  }
}
