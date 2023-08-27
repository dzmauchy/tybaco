package org.tybaco.ui.lib.tabs;

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

import lombok.Getter;
import lombok.Setter;
import org.intellij.lang.annotations.MagicConstant;

import javax.swing.*;
import java.awt.*;
import java.util.function.IntConsumer;

import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_TAB_CLOSABLE;
import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_TAB_CLOSE_CALLBACK;

public abstract class CloseableTabPane extends JTabbedPane {

  private final IntConsumer onTabClose = this::onTabClose;

  @Getter
  @Setter
  private boolean defaultCloseable = true;

  public CloseableTabPane() {
    this(TOP, SCROLL_TAB_LAYOUT);
  }

  public CloseableTabPane(
    @MagicConstant(intValues = {TOP, BOTTOM, LEFT, RIGHT}) int tabPlacement,
    @MagicConstant(intValues = {SCROLL_TAB_LAYOUT, WRAP_TAB_LAYOUT}) int tabLayoutPolicy) {
    super(tabPlacement, tabLayoutPolicy);
    model.addChangeListener(e -> onTabSelection(getSelectedIndex()));
  }

  @Override
  public void addTab(String title, Component component) {
    prepareComponent(component);
    super.addTab(title, component);
  }

  @Override
  public void addTab(String title, Icon icon, Component component) {
    prepareComponent(component);
    super.addTab(title, icon, component);
  }

  @Override
  public void addTab(String title, Icon icon, Component component, String tip) {
    prepareComponent(component);
    super.addTab(title, icon, component, tip);
  }

  @Override
  public void insertTab(String title, Icon icon, Component component, String tip, int index) {
    prepareComponent(component);
    super.insertTab(title, icon, component, tip, index);
  }

  public void setTabCloseable(int index, boolean closeable) {
    setTabCloseable(getComponentAt(index), closeable);
  }

  public void setTabCloseable(Component component, boolean closeable) {
    if (component instanceof JComponent c) {
      c.putClientProperty(TABBED_PANE_TAB_CLOSABLE, closeable);
    }
  }

  private void prepareComponent(Component component) {
    if (component instanceof JComponent c) {
      prepareComponent(c);
    }
  }

  protected void prepareComponent(JComponent component) {
    component.putClientProperty(TABBED_PANE_TAB_CLOSE_CALLBACK, onTabClose);
    component.putClientProperty(TABBED_PANE_TAB_CLOSABLE, defaultCloseable);
  }

  protected void onTabClose(int index) {
  }

  protected void onTabSelection(int index) {
  }
}
