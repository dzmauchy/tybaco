package org.tybloco.ui.main;

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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.tybloco.logging.Log;
import org.tybloco.ui.Main;
import org.tybloco.ui.lib.logging.UILogHandler;
import org.tybloco.ui.lib.stage.StageLinuxBugListener;

public class MainApplication extends Application {

  private final MainApplicationContext context = new MainApplicationContext();
  private final Thread shutdownHook = new Thread(context::close);

  private static void updateSplash() {
    Main.updateSplash.run();
  }

  private static void updateSplashStatus() {
    Main.updateSplashStatus.run();
  }

  @Override
  public void init() throws Exception {
    initFont();
    updateSplash();
    Application.setUserAgentStylesheet(STYLESHEET_MODENA);
    updateSplash();
    Platform.runLater(MainApplication::initLaf);
    Runtime.getRuntime().addShutdownHook(shutdownHook);
  }

  @Override
  public void stop() {
    try (context) {
      context.stop();
    } finally {
      Runtime.getRuntime().removeShutdownHook(shutdownHook);
    }
  }

  @Override
  public void start(Stage stage) {
    try {
      updateSplash();
      UILogHandler.getInstance().flush();
      stage.setTitle("Tybloco IDE");
      stage.setUserData("primary");
      updateSplash();
      context.getDefaultListableBeanFactory().registerSingleton("primaryStage", stage);
      context.register(MainConfiguration.class);
      updateSplash();
      context.refresh();
      updateSplash();
      stage.getIcons().add(new Image("icon/project.png"));
      updateSplash();
      var mainPane = context.getBean(MainPane.class);
      updateSplash();
      stage.setScene(new Scene(mainPane, 1024, 768, Color.BLACK));
      stage.setMaximized(true);
      StageLinuxBugListener.install(stage);
      stage.show();
      updateSplash();
      context.start();
      updateSplash();
      updateSplashStatus();
    } catch (Throwable e) {
      try (context) {
        stage.close();
      } catch (Throwable x) {
        e.addSuppressed(x);
      }
      throw e;
    }
  }

  private static void initLaf() {
    com.sun.javafx.css.StyleManager.getInstance().addUserAgentStylesheet("theme/ui.css");
    updateSplash();
  }

  private static void initFont() throws Exception {
    var classLoader = Thread.currentThread().getContextClassLoader();
    try (var is = classLoader.getResourceAsStream("META-INF/fonts/NotoSans-Regular.ttf")) {
      if (is != null) {
        var field = Font.class.getDeclaredField("DEFAULT");
        if (field.trySetAccessible()) {
          field.set(null, Font.loadFont(is, 13d));
          Log.info(MainApplication.class, "Custom font loaded");
        }
      }
    }
  }
}
