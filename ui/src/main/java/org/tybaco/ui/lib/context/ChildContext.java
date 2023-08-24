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
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

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

  public <W extends Window> W child(String id, String name, Class<W> type, AnnotationConfigApplicationContext ctx, Consumer<ChildContext> consumer) {
    var child = new ChildContext(id, name, ctx);
    consumer.accept(child);
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
          logger.error("Window close error", x);
        }
        w.removeWindowListener(this);
      }
    });
    w.setVisible(true);
    return w;
  }
}
