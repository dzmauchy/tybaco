package org.tybloco.ide.splash;

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
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.locks.LockSupport;

import static javafx.application.Platform.runLater;

public class SplashPreloader extends Preloader {

  @Override
  public void start(Stage stage) {
    stage.initStyle(StageStyle.UNDECORATED);
    stage.setAlwaysOnTop(true);
    var canvas = new Canvas(800, 533);
    var gc = canvas.getGraphicsContext2D();
    gc.drawImage(new Image("images/logo.jpg"), 0, 0);
    gc.setFill(Color.WHITE);
    try (var is = SplashPreloader.class.getClassLoader().getResourceAsStream("fonts/hs.ttf")) {
      var font = Font.loadFont(is, 80);
      gc.setFont(font);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    gc.setStroke(Color.WHITE);
    gc.setLineWidth(2d);
    gc.fillText("Tybloco IDE", 20, 100);
    gc.strokeLine(20, 130, 700, 130);
    gc.setFont(new Font(gc.getFont().getName(), 48));
    gc.fillText("A visual microservice IDE", 20, 200);
    stage.setScene(new Scene(new Group(canvas)));
    var thread = new Thread(new ThreadGroup("preloader"), () -> {
      var lastStep = SplashStatus.step.get();
      while (!SplashStatus.finished) {
        var step = SplashStatus.step.get();
        if (lastStep == step) {
          Thread.yield();
          continue;
        }
        lastStep = step;
        var maxStep = Math.max(SplashStatus.maxStep(), step);
        runLater(() -> {
          var w = (canvas.getWidth() * step) / maxStep;
          gc.fillRect(0, canvas.getHeight() - 8, w, 8);
        });
      }
      LockSupport.parkNanos(1_000_000_000L);
      Platform.runLater(stage::close);
    }, "preloader-waiter");
    thread.setDaemon(true);
    thread.start();
    stage.show();
  }
}
