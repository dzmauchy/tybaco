package org.tybaco.ui.child.project;

import javafx.stage.FileChooser;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tybaco.editors.text.Texts;
import org.tybaco.ui.main.MainStage;
import org.tybaco.ui.model.Project;

import java.io.File;
import java.util.Optional;
import java.util.prefs.Preferences;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Scope(SCOPE_PROTOTYPE)
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
