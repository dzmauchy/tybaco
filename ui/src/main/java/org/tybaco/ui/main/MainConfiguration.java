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
import org.tybaco.ui.lib.actions.SmartAction;

import static org.tybaco.ui.lib.context.ChildContext.child;
import static org.tybaco.ui.lib.window.Windows.findWindow;

@ComponentScan(lazyInit = true)
@Component
public class MainConfiguration {

  @Bean
  @Qualifier("log")
  public SmartAction groupZ_showLogFrameAction(AnnotationConfigApplicationContext context) {
    return new SmartAction("showLogs", "Show logs", "icon/logs.svg", e -> {
      var frame = findWindow(LogFrame.class).orElseGet(() -> child("logs", "Logs", LogFrame.class, context));
      frame.setVisible(true);
      frame.toFront();
    });
  }
}
