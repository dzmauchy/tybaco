package org.tybaco.ui.lib.context;

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

import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.context.support.GenericApplicationContext;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;
import static org.springframework.context.support.AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME;

public interface Propagated {

  static void installErrorHandler(GenericApplicationContext ctx) {
    var f = ctx.getDefaultListableBeanFactory();
    if (f.getSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME) instanceof SimpleApplicationEventMulticaster m) {
      m.setErrorHandler(e -> getLogger(ctx.getId()).log(SEVERE, "Multicaster error", e));
    }
  }
}
