package org.tybloco.ui.lib.context;

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
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.tybloco.ui.lib.logging.LogBeanPostProcessor;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public final class ChildContext extends AnnotationConfigApplicationContext {

  private final AnnotationConfigApplicationContext parent;
  private final ApplicationListener<?> parentEventListener;
  private final LogBeanPostProcessor logBeanPostProcessor;

  public ChildContext(String id, String name, AnnotationConfigApplicationContext parent) {
    this.parent = parent;
    this.parentEventListener = event -> {
      if (event instanceof Propagated || event instanceof PayloadApplicationEvent<?>) {
        publishEvent(event);
      } else if (event instanceof ContextClosedEvent) {
        parent.removeApplicationListener(ChildContext.this.parentEventListener);
        close();
      } else if (event instanceof ContextStoppedEvent) {
        stop();
      }
    };
    this.logBeanPostProcessor = new LogBeanPostProcessor(id);
    getDefaultListableBeanFactory().setParentBeanFactory(parent.getDefaultListableBeanFactory());
    setId(id);
    setDisplayName(name);
    getEnvironment().merge(parent.getEnvironment());
    setAllowBeanDefinitionOverriding(false);
    setAllowCircularReferences(false);
    parent.addApplicationListener(parentEventListener);
    addApplicationListener(ev -> logBeanPostProcessor.info("{0}", ev));
  }

  @Override
  protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    beanFactory.addBeanPostProcessor(logBeanPostProcessor);
    super.prepareBeanFactory(beanFactory);
  }

  @Override
  protected void onClose() {
    logBeanPostProcessor.info("Closing");
    parent.removeApplicationListener(parentEventListener);
  }

  @Override
  protected void onRefresh() throws BeansException {
    logBeanPostProcessor.info("Refreshing");
    Propagated.installErrorHandler(this);
  }

  @Override
  public void stop() {
    logBeanPostProcessor.info("Stopping");
    super.stop();
  }

  @Override
  protected MessageSource getInternalParentMessageSource() {
    var pbf = requireNonNull(getDefaultListableBeanFactory()).getParentBeanFactory();
    if (pbf instanceof DefaultListableBeanFactory f) {
      return (MessageSource) f.getBean(MESSAGE_SOURCE_BEAN_NAME);
    } else {
      return super.getInternalParentMessageSource();
    }
  }

  public <T> T refreshAndStart(Function<ChildContext, T> refresh) {
    try {
      refresh();
      var t = refresh.apply(this);
      start();
      return t;
    } catch (Throwable e) {
      try (this) {
        throw e;
      } catch (Throwable x) {
        e.addSuppressed(x);
        throw e;
      }
    }
  }
}
