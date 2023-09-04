package org.tybaco.ui.child.project;

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
import javafx.concurrent.Worker.State;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.tybaco.ui.main.services.ProjectServer;
import org.tybaco.ui.model.Project;

import java.util.logging.Logger;

import static java.util.logging.Level.*;

@Component
public class ProjectDiagram {

  private static final Logger LOG = Logger.getLogger(ProjectDiagram.class.getName());

  @Bean
  public WebView projectWebView(Project project, ProjectServer server, ProjectWebUserData data) {
    var view = new WebView();
    view.setContextMenuEnabled(true);
    view.setPageFill(Color.BLACK);
    var engine = view.getEngine();
    var loadWorker = engine.getLoadWorker();
    final class Listener implements ChangeListener<State> {
      @Override
      public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
        switch (newValue) {
          case SUCCEEDED -> {
            LOG.log(INFO, "Document loaded");
            view.setUserData(engine.executeScript("window"));
            LOG.log(INFO, "Window initialized {0}", view.getUserData());
            loadWorker.stateProperty().removeListener(this);
          }
          case FAILED -> {
            LOG.log(SEVERE, "Document load failed", loadWorker.getException());
            loadWorker.stateProperty().removeListener(this);
          }
          case CANCELLED -> {
            LOG.log(WARNING, "Document load cancelled");
            loadWorker.stateProperty().removeListener(this);
          }
        }
      }
    }
    loadWorker.stateProperty().addListener(new Listener());
    engine.load(server.projectUrl(project));
    engine.setUserDataDirectory(data.directory.toFile());
    engine.setJavaScriptEnabled(true);
    return view;
  }
}
