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

import javafx.stage.FileChooser;
import org.springframework.stereotype.Component;
import org.tybloco.editors.text.Texts;
import org.tybloco.ui.main.MainStage;
import org.tybloco.ui.model.Project;

import java.io.File;
import java.util.Optional;
import java.util.prefs.Preferences;

@Component
public class ProjectSaveDialog {

  private static final Preferences PREFERENCES = Preferences.userNodeForPackage(ProjectSaveDialog.class);

  private final Project project;

  public ProjectSaveDialog(Project project) {
    this.project = project;
  }

  public Optional<File> showAndWait() {
    var oldDir = new File(PREFERENCES.get("saveDir", System.getProperty("user.dir")));
    var chooser = new FileChooser();
    chooser.setInitialDirectory(oldDir.isDirectory() ? oldDir : new File(System.getProperty("user.dir")));
    chooser.setInitialFileName(project.name.get() + ".xml");
    chooser.titleProperty().bind(Texts.text("Save the project"));
    return Optional.ofNullable(chooser.showSaveDialog(MainStage.mainStage())).map(d -> {
      PREFERENCES.put("saveDir", d.getParent());
      return d;
    });
  }
}
