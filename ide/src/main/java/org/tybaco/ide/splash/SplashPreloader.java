package org.tybaco.ide.splash;

/*-
 * #%L
 * ide
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
import javafx.application.Preloader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Timer;
import java.util.TimerTask;

public class SplashPreloader extends Preloader {

  @Override
  public void start(Stage stage) {
    stage.initStyle(StageStyle.UNDECORATED);
    var canvas = new Canvas(800, 533);
    var gc = canvas.getGraphicsContext2D();
    gc.drawImage(new Image("images/logo.jpg"), 0, 0);
    gc.setFill(Color.WHITE);
    stage.setScene(new Scene(new Group(canvas)));
    var timer = new Timer("preloader", true);
    var task = new TimerTask() {

      private int lastStep = SplashStatus.step();

      @Override
      public void run() {
        if (SplashStatus.finished) {
          Platform.runLater(stage::close);
          return;
        }
        var step = SplashStatus.step();
        if (step <= lastStep) {
          return;
        }

        var lastMaxStep = SplashStatus.maxStep();
        var maxStep = Math.max(lastMaxStep, step);

        Platform.runLater(() -> {
          var w = (canvas.getWidth() * step) / maxStep;
          gc.fillRect(0, canvas.getHeight() - 8, w, 8);
        });
      }
    };
    stage.setOnHidden(e -> timer.cancel());
    timer.scheduleAtFixedRate(task, 0L, 5L);
    stage.show();
  }
}
