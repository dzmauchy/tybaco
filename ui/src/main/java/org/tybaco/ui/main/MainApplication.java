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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.tybaco.ui.Main;
import org.tybaco.ui.lib.action.Actions;
import org.tybaco.ui.lib.logging.UILogHandler;

import static java.lang.Thread.currentThread;

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
    Platform.runLater(MainApplication::initLaf);
  }

  @Override
  public void start(Stage stage) {
    try {
      updateSplash();
      UILogHandler.getInstance().flush();
      updateSplash();
      context.getDefaultListableBeanFactory().registerSingleton("primaryStage", stage);
      context.register(MainPane.class);
      context.registerBean(Actions.class, () -> new Actions(context));
      updateSplash();
      context.refresh();
      updateSplash();
      stage.getIcons().add(new Image("icon/project.png"));
      updateSplash();
      var mainPane = context.getBean(MainPane.class);
      updateSplash();
      stage.setScene(new Scene(mainPane, 800, 600));
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

  private static void initLaf() {
    try {
      var styleManagerClass = currentThread().getContextClassLoader().loadClass("com.sun.javafx.css.StyleManager");
      var instance = styleManagerClass.getMethod("getInstance").invoke(null);
      var addUserAgentStylesheet = styleManagerClass.getMethod("addUserAgentStylesheet", String.class);
      addUserAgentStylesheet.invoke(instance, "theme/ui.css");
    } catch (Exception e) {
      throw new IllegalStateException(e);
    } finally {
      updateSplash();
    }
  }
}
