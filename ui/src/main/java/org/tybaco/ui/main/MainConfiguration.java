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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.tybaco.ui.child.logging.LogFrame;
import org.tybaco.ui.child.logging.LogFrameConfig;
import org.tybaco.ui.lib.actions.SmartAction;
import org.tybaco.ui.lib.context.ChildContext;

import java.awt.*;

@ComponentScan(lazyInit = true)
@Component
public class MainConfiguration {

    @Bean
    @Qualifier("log")
    public SmartAction groupZ_showLogFrameAction(AnnotationConfigApplicationContext context) {
        return new SmartAction("showLogs", "Show logs", e -> {
            var windows = Window.getWindows();
            for (var window : windows) {
                if (window instanceof LogFrame f) {
                    f.setVisible(true);
                    f.toFront();
                    return;
                }
            }
            var ctx = new ChildContext("logs", "Logs", context);
            ctx.register(LogFrameConfig.class);
            ctx.refresh();
            var frame = ctx.getBean(LogFrame.class);
            assert frame != null;
            frame.setVisible(true);
        });
    }
}
