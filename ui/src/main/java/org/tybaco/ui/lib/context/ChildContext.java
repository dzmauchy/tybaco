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

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.*;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

public final class ChildContext extends AnnotationConfigApplicationContext {

  public ChildContext(String id, String name, AnnotationConfigApplicationContext parent) {
    requireNonNull(getDefaultListableBeanFactory()).setParentBeanFactory(parent.getDefaultListableBeanFactory());
    setId(id);
    setDisplayName(name);
    requireNonNull(getEnvironment()).merge(parent.getEnvironment());
    setAllowBeanDefinitionOverriding(false);
    setAllowCircularReferences(false);
    var parentEventListener = (ApplicationListener<?>) event -> {
      if (event instanceof Propagated || event instanceof PayloadApplicationEvent<?>) {
        publishEvent(event);
      }
    };
    parent.addApplicationListener(parentEventListener);
    addApplicationListener(event -> {
      if (event instanceof ContextClosedEvent) {
        parent.removeApplicationListener(parentEventListener);
      } else if (event instanceof ContextRefreshedEvent ev) {
        var ctx = (ChildContext) ev.getApplicationContext();
        assert ctx != null;
        var beanFactory = ctx.getDefaultListableBeanFactory();
        assert beanFactory != null;
        var multicaster = beanFactory.getSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME);
        if (multicaster instanceof SimpleApplicationEventMulticaster m) {
          m.setErrorHandler(e -> getLogger(id).log(SEVERE, "Unknown exception", e));
        }
      }
    });
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

  @SafeVarargs
  public static <W extends Window> W child(String id, String name, Class<W> type, AnnotationConfigApplicationContext ctx, Consumer<ChildContext>... consumers) {
    var child = new ChildContext(id, name, ctx);
    child.register(type);
    for (var consumer : consumers) {
      consumer.accept(child);
    }
    try {
      child.refresh();
      var w = child.getBean(type);
      assert w != null;
      w.addWindowListener(new WindowAdapter() {
        @Override
        public void windowOpened(WindowEvent e) {
          child.start();
        }

        @Override
        public void windowClosed(WindowEvent e) {
          try (child) {
            child.stop();
          } catch (Throwable x) {
            getLogger(id).log(WARNING, "Window close error", x);
          } finally {
            w.removeWindowListener(this);
          }
        }
      });
      return w;
    } catch (Throwable e) {
      try (child) {
        child.stop();
      } catch (Throwable x) {
        e.addSuppressed(x);
      }
      getLogger(id).log(SEVERE, "Child error", e);
      throw e;
    }
  }
}
