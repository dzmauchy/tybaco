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

import com.sun.javafx.PlatformUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.tybaco.ui.Main;
import org.tybaco.ui.lib.logging.UILogHandler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

import static org.tybaco.logging.Log.info;

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
  public void init() {
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
      stage.setTitle("Tybaco IDE");
      stage.setFullScreenExitKeyCombination(new KeyCodeCombination(KeyCode.ESCAPE));
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
      if (PlatformUtil.isLinux()) { // a workaround of a bug of modal dialogs shown on top of the stage
        stage.setAlwaysOnTop(true);
        stage.addEventHandler(WindowEvent.WINDOW_SHOWN, new EventHandler<>() {
          @Override
          public void handle(WindowEvent windowEvent) {
            stage.removeEventHandler(WindowEvent.WINDOW_SHOWN, this);
            Thread.startVirtualThread(() -> {
              doubleClick(stage);
              LockSupport.parkNanos(10_000_000L);
              doubleClick(stage);
              Platform.runLater(() -> stage.setAlwaysOnTop(false));
            });
          }
        });
      }
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

  private static void doubleClick(Stage stage) {
    robotAction(stage, r -> r.mouseClick(MouseButton.PRIMARY));
    LockSupport.parkNanos(1_000_000L);
    robotAction(stage, r -> r.mouseClick(MouseButton.PRIMARY));
  }

  private static void robotAction(Stage stage, Consumer<Robot> action) {
    var latch = new CountDownLatch(1);
    Platform.runLater(() -> {
      try {
        var robot = new Robot();
        var centerX = stage.getX() + stage.getWidth() / 2d;
        robot.mouseMove(centerX, stage.getY() + 3d);
        action.accept(robot);
      } finally {
        latch.countDown();
      }
    });
    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }
}
