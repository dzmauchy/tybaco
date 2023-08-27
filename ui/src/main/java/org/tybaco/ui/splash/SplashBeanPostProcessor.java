package org.tybaco.ui.splash;

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
import org.tybaco.ui.lib.logging.Logging;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.LogManager;

import static org.tybaco.ui.splash.Splash.updateSplash;
import static org.tybaco.ui.splash.SplashStatus.incrementStep;

public class SplashBeanPostProcessor implements DestructionAwareBeanPostProcessor {

  private final AtomicLong lastTime = new AtomicLong(System.nanoTime());

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) {
    info("Initializing {0}", beanName);
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) {
    incrementStep();
    var time = System.nanoTime();
    var nextTime = lastTime.updateAndGet(t -> time - t > 100_000_000L ? time : t);
    if (nextTime == time) {
      updateSplash(false);
    }
    info("Initialized {0}", beanName);
    return bean;
  }

  @Override
  public void postProcessBeforeDestruction(Object bean, String beanName) {
    info("Destructing {0}", beanName);
  }

  private void info(String message, Object... args) {
    var info = Logging.info(message, args);
    info.setSourceClassName(null);
    info.setLoggerName("init");
    LogManager.getLogManager().getLogger("").log(info);
  }
}
