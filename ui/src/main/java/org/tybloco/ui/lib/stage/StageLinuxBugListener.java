package org.tybloco.ui.lib.stage;

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

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.robot.Robot;
import javafx.stage.*;
import org.tybloco.util.FastLatch;

import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

public class StageLinuxBugListener implements EventHandler<WindowEvent> {
  @Override
  public void handle(WindowEvent event) {
    var window = (Window) event.getSource();
    window.removeEventHandler(WindowEvent.WINDOW_SHOWN, this);
    if (window instanceof Stage s) {
      s.setAlwaysOnTop(true);
    }
    Thread.startVirtualThread(() -> {
      doubleClick(window);
      LockSupport.parkNanos(10_000_000L);
      doubleClick(window);
      if (window instanceof Stage s) {
        Platform.runLater(() -> s.setAlwaysOnTop(false));
      }
    });
  }

  private void doubleClick(Window window) {
    robotAction(window, r -> r.mouseClick(MouseButton.PRIMARY));
    LockSupport.parkNanos(1_000_000L);
    robotAction(window, r -> r.mouseClick(MouseButton.PRIMARY));
  }

  private void robotAction(Window stage, Consumer<Robot> action) {
    var latch = new FastLatch(1);
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
    latch.await();
  }

  public static void install(Stage stage) {
    if (com.sun.javafx.PlatformUtil.isLinux()) {
      stage.addEventHandler(WindowEvent.WINDOW_SHOWN, new StageLinuxBugListener());
    }
  }
}
