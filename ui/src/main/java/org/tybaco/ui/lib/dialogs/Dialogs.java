package org.tybaco.ui.lib.dialogs;

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

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.showInputDialog;
import static org.tybaco.ui.lib.images.ImageCache.smartIcon;

public final class Dialogs {

  private Dialogs() {
  }

  @SuppressWarnings("unchecked")
  public static <T> T input(Component owner, String message, String title, String icon, T defaultValue) {
    var smartIcon = smartIcon(icon, 24);
    return (T) showInputDialog(owner, message, title, QUESTION_MESSAGE, smartIcon, null, defaultValue);
  }

  public static <T> void input(Component owner, String message, String title, String icon, T defaultValue, Consumer<T> consumer) {
    var v = input(owner, message, title, icon, defaultValue);
    if (v != null) {
      consumer.accept(v);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T input(Component owner, String message, String title, String icon, List<T> options, T init) {
    var smartIcon = smartIcon(icon, 24);
    return (T) showInputDialog(owner, message, title, QUESTION_MESSAGE, smartIcon, options.toArray(), init);
  }

  public static <T> void input(Component owner, String message, String title, String icon, List<T> options, T init, Consumer<T> consumer) {
    var v = input(owner, message, title, icon, options, init);
    if (v != null) {
      consumer.accept(v);
    }
  }
}
