package org.tybaco.ui.lib.actions;

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

import org.springframework.beans.factory.ObjectProvider;
import org.tybaco.ui.lib.images.ImageCache;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.TreeMap;

import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public final class SmartAction extends AbstractAction {

  private static final String GROUP_KEY = "actionGroup";
  public static final int SMALL_ICON_SIZE = 18;
  public static final int LARGE_ICON_SIZE = 24;
  public static final ActionListener EMPTY_ACTION = e -> {};

  private final ActionListener action;

  private SmartAction(String cmd, ActionListener... actions) {
    action = merge(actions);
    putValue(ACTION_COMMAND_KEY, cmd);
  }

  public SmartAction(String cmd, String name, ActionListener... actions) {
    this(cmd, actions);
    putValue(NAME, name);
  }

  public SmartAction(String cmd, String name, String icon, ActionListener... actions) {
    this(cmd, name, actions);
    putValue(SMALL_ICON, smartIcon(icon, SMALL_ICON_SIZE));
    putValue(LARGE_ICON_KEY, smartIcon(icon, LARGE_ICON_SIZE));
  }

  public SmartAction(String cmd, String name, String icon, KeyStroke accelerator, ActionListener... actions) {
    this(cmd, name, icon, actions);
    putValue(ACCELERATOR_KEY, accelerator);
  }

  public SmartAction name(String name) {
    putValue(NAME, name);
    return this;
  }

  public SmartAction shortDescription(String description) {
    putValue(SHORT_DESCRIPTION, description);
    return this;
  }

  public SmartAction longDescription(String description) {
    putValue(LONG_DESCRIPTION, description);
    return this;
  }

  public SmartAction icon(String icon) {
    putValue(SMALL_ICON, ImageCache.icon(icon, SMALL_ICON_SIZE));
    putValue(LARGE_ICON_KEY, ImageCache.icon(icon, LARGE_ICON_SIZE));
    return this;
  }

  public SmartAction svgIcon(String icon) {
    putValue(SMALL_ICON, ImageCache.svgIcon(icon, SMALL_ICON_SIZE));
    putValue(LARGE_ICON_KEY, ImageCache.svgIcon(icon, LARGE_ICON_SIZE));
    return this;
  }

  public SmartAction accelerator(KeyStroke keyStroke) {
    putValue(ACCELERATOR_KEY, keyStroke);
    return this;
  }

  public SmartAction accelerator(String keyStroke) {
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyStroke));
    return this;
  }

  public SmartAction selected(Boolean selected) {
    putValue(SELECTED_KEY, selected);
    return this;
  }

  public SmartAction enabled(boolean enabled) {
    setEnabled(enabled);
    return this;
  }

  public SmartAction group(String group) {
    putValue(GROUP_KEY, group);
    return this;
  }

  public String getGroup() {
    return requireNonNullElse(getValue(GROUP_KEY), "").toString();
  }

  private static ActionListener merge(ActionListener[] actions) {
    return switch (actions.length) {
      case 0 -> EMPTY_ACTION;
      case 1 -> actions[0];
      default -> e -> {
        for (var action : actions) {
          action.actionPerformed(e);
        }
      };
    };
  }

  private static ImageIcon smartIcon(String icon, int size) {
    return icon.endsWith(".svg") ? ImageCache.svgIcon(icon, size) : ImageCache.icon(icon, size);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    action.actionPerformed(e);
  }

  public static TreeMap<String, List<SmartAction>> group(ObjectProvider<SmartAction> actions) {
    return actions.orderedStream().collect(groupingBy(SmartAction::getGroup, TreeMap::new, toList()));
  }
}
