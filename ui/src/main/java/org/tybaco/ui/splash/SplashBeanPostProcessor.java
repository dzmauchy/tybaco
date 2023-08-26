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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.concurrent.atomic.AtomicLong;

import static org.tybaco.ui.splash.Splash.updateSplash;
import static org.tybaco.ui.splash.SplashStatus.incrementStep;

public class SplashBeanPostProcessor implements BeanPostProcessor {

  private final AtomicLong lastTime = new AtomicLong(System.nanoTime());

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    incrementStep();
    var time = System.nanoTime();
    var nextTime = lastTime.updateAndGet(t -> time - t > 100_000_000L ? time : t);
    if (nextTime == time) {
      updateSplash(false);
    }
    return bean;
  }
}
