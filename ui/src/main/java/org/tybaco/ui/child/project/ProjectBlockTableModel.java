package org.tybaco.ui.child.project;

import org.jetbrains.annotations.Nls;
import org.springframework.stereotype.Component;
import org.tybaco.ui.lib.tables.TableListModelAdapter;
import org.tybaco.ui.main.projects.Block;
import org.tybaco.ui.main.projects.Project;

@Component
public final class ProjectBlockTableModel extends TableListModelAdapter<Block> {

  public ProjectBlockTableModel(Project project) {
    super(project.blocks);
  }

  @Override
  protected Object getValueAt(Block element, int columnIndex) {
    return switch (columnIndex) {
      case 0 -> element.id;
      case 1 -> element.name.get();
      case 2 -> element.factory + "." + element.selector;
      default -> throw new IndexOutOfBoundsException(columnIndex);
    };
  }

  @Override
  public int getColumnCount() {
    return 3;
  }

  @Nls
  @Override
  public String getColumnName(int columnIndex) {
    return switch (columnIndex) {
      case 0 -> "Id";
      case 1 -> "Name";
      case 2 -> "Type";
      default -> throw new IndexOutOfBoundsException(columnIndex);
    };
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return switch (columnIndex) {
      case 0 -> int.class;
      case 1, 2 -> String.class;
      default -> throw new IndexOutOfBoundsException(columnIndex);
    };
  }
}
