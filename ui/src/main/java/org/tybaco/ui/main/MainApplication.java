package org.tybaco.ui.main;

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

import com.sun.javafx.css.StyleManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.tybaco.ui.Main;
import org.tybaco.ui.lib.logging.UILogHandler;

public class MainApplication extends Application {

  private final MainApplicationContext context = new MainApplicationContext();

  private static void updateSplash() {
    Main.updateSplash.run();
  }

  private static void updateSplashStatus() {
    Main.updateSplashStatus.run();
  }

  @Override
  public void init() {
    updateSplash();
    Application.setUserAgentStylesheet(STYLESHEET_MODENA);
    updateSplash();
    Platform.runLater(() -> {
      var styleManager = StyleManager.getInstance();
      styleManager.addUserAgentStylesheet("theme/ui.css");
      updateSplash();
    });
  }

  @Override
  public void start(Stage stage) {
    try {
      updateSplash();
      UILogHandler.getInstance().flush();
      updateSplash();
      context.refresh();
      updateSplash();
      stage.setScene(new Scene(new TabPane(), 800, 600));
      stage.setMaximized(true);
      stage.show();
      updateSplash();
      context.start();
      updateSplash();
      updateSplashStatus();
    } catch (Throwable e) {
      stage.close();
      throw e;
    }
  }
}
