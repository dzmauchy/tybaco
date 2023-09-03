package org.tybaco.ui.lib.action;

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

import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.*;
import java.util.function.Consumer;

public final class Actions {

  private final AnnotationConfigApplicationContext context;

  public Actions(AnnotationConfigApplicationContext context) {
    this.context = context;
  }

  @SafeVarargs
  public final void fillMenu(Menu menu, Map<String, Action> actions, Consumer<MenuItem>... consumers) {
    var grouped = new TreeMap<String, LinkedList<Action>>();
    actions.forEach((beanName, action) -> {
      if (context.getBeanDefinition(beanName) instanceof AnnotatedBeanDefinition d) {
        var md = d.getFactoryMethodMetadata();
        if (md != null) {
          var annotations = md.getAnnotations();
          var actionBean = annotations.get(ActionBean.class);
          if (actionBean.isPresent()) {
            var group = actionBean.getString("group");
            grouped.computeIfAbsent(group, k -> new LinkedList<>()).addLast(action);
          }
        }
      }
    });
    var menuItems = menu.getItems();
    grouped.forEach((group, batch) -> {
      if (!group.equals(grouped.firstKey())) {
        menuItems.add(new SeparatorMenuItem());
      }
      batch.forEach(action -> menuItems.add(action.toMenuItem(consumers)));
    });
  }
}
