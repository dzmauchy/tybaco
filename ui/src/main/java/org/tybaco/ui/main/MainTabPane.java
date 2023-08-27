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

import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.tybaco.ui.lib.context.ChildContext;
import org.tybaco.ui.lib.tabs.CloseableTabPane;

import javax.swing.*;
import java.util.IdentityHashMap;
import java.util.function.Consumer;

import static java.util.logging.Level.INFO;
import static org.tybaco.ui.lib.images.ImageCache.svgIcon;
import static org.tybaco.ui.lib.logging.Logging.info;
import static org.tybaco.ui.lib.logging.Logging.warn;

@Component
@Log
@AllArgsConstructor
public class MainTabPane extends CloseableTabPane {

  private final IdentityHashMap<Object, ChildContext> contexts = new IdentityHashMap<>();
  private final AnnotationConfigApplicationContext parent;

  @SafeVarargs
  public final <T extends JComponent> T tab(String id, String name, Class<T> tabType, Consumer<ChildContext>... consumers) {
    for (int i = 0; i < getTabCount(); i++) {
      if (getTabComponentAt(i) instanceof JComponent c && id.equals(c.getClientProperty("TY_TAB_ID"))) {
        setSelectedIndex(i);
        return tabType.cast(c);
      }
    }
    var child = new ChildContext(id, name, parent);
    child.register(tabType);
    for (var consumer : consumers) {
      consumer.accept(child);
    }
    try {
      child.refresh();
      var component = child.getBean(tabType);
      assert component != null;
      component.putClientProperty("TY_TAB_ID", id);
      addTab(name, svgIcon("icon/project.svg", 18), component);
      child.start();
      contexts.put(component, child);
      return component;
    } catch (Throwable e) {
      log.log(warn("Unable to refresh the context {}", e, child));
      try (child) {
        child.stop();
        throw e;
      }
    }
  }

  @Override
  protected void onTabClose(int index) {
    var component = getTabComponentAt(index);
    removeTabAt(index);
    var context = contexts.remove(component);
    try (context) {
      log.log(INFO, "Closing {}", context);
    } catch (Throwable e) {
      log.log(info("Context {} closing error", e, context));
    }
  }
}
