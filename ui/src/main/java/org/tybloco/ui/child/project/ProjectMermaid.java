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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.tybloco.ui.main.project.ProjectServer;
import org.tybloco.ui.model.Project;

import java.util.logging.Logger;

import static java.util.logging.Level.*;

@Component
public class ProjectMermaid {

  private static final Logger LOG = Logger.getLogger(ProjectMermaid.class.getName());

  private final WebView view = new WebView();

  public ProjectMermaid(ProjectWebUserData data, Project project, ProjectServer server) {
    view.setContextMenuEnabled(true);
    view.setPageFill(Color.BLACK);
    var engine = view.getEngine();
    engine.setUserDataDirectory(data.directory.toFile());
    engine.setJavaScriptEnabled(true);
    var loadWorker = engine.getLoadWorker();
    loadWorker.stateProperty().addListener(new WebViewStateListener());
    view.getEngine().load(server.projectUrl(project));
  }

  @Lazy
  @Bean
  public WebView view() {
    return view;
  }

  private final class WebViewStateListener implements ChangeListener<State> {

    private final Worker<Void> loadWorker = view.getEngine().getLoadWorker();

    @Override
    public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
      switch (newValue) {
        case SUCCEEDED -> LOG.log(INFO, "Document loaded");
        case FAILED -> LOG.log(SEVERE, "Document load failed", loadWorker.getException());
        case CANCELLED -> LOG.log(WARNING, "Document load cancelled");
        case READY -> LOG.log(INFO, "WebView is ready");
        case RUNNING -> LOG.log(INFO, "Document loading");
        case SCHEDULED -> LOG.log(INFO, "Document scheduled");
      }
    }
  }
}
