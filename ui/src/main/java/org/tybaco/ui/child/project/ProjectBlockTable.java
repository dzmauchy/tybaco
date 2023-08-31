package org.tybaco.ui.child.project;

import org.tybaco.ui.lib.context.UIComponent;
import org.tybaco.ui.lib.tables.Tables;

import javax.swing.*;

@UIComponent
public final class ProjectBlockTable extends JTable {

  public ProjectBlockTable(ProjectBlockTableModel model) {
    super(model);
    Tables.initColumns(this, 75, 225, 300);
  }
}
