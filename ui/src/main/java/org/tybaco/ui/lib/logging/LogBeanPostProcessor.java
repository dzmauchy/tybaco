package org.tybaco.ui.lib.logging;

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

import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;

import java.util.logging.LogManager;

public final class LogBeanPostProcessor implements DestructionAwareBeanPostProcessor {

  private final String id;

  public LogBeanPostProcessor(String id) {
    this.id = id;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) {
    info("Initializing {0}", beanName);
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) {
    info("Initialized {0}", beanName);
    return bean;
  }

  @Override
  public void postProcessBeforeDestruction(Object bean, String beanName) {
    info("Destructing {0}", beanName);
  }

  public void info(String message, Object... args) {
    var info = Logging.info(message, args);
    info.setSourceClassName(null);
    info.setLoggerName(id);
    LogManager.getLogManager().getLogger("").log(info);
  }
}
