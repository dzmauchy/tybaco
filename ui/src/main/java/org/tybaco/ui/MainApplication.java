package org.tybaco.ui;

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
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.java.Log;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.tybaco.ui.main.MainConfiguration;

import static javafx.application.Platform.runLater;

@Log
public class MainApplication extends Application {

    private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    @Override
    public void init() {
        runLater(() -> {
            var styleManager = StyleManager.getInstance();
            setUserAgentStylesheet(STYLESHEET_MODENA);
            styleManager.addUserAgentStylesheet("/skins/override.css");
        });
        context.register(MainConfiguration.class);
        context.refresh();
    }

    @Override
    public void start(Stage stage) {
        var scene = new Scene(new TabPane(new Tab("a", new Pane())), 800, 600);
        stage.setScene(scene);
        stage.initStyle(StageStyle.DECORATED);
        stage.setMaximized(true);
        stage.setTitle("Tybaco IDE");
        context.start();
        stage.show();
    }

    @Override
    public void stop() {
        try (context) {
            context.stop();
        }
    }
}
