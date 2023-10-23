package org.tybloco.editors.font;

/*-
 * #%L
 * editors
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

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.Objects;
import java.util.stream.IntStream;

public class FontViewerFrame extends JFrame {

  static final Font FONT = loadFont();
  static final int[] CPS = IntStream.range(256, 1 << 20)
    .filter(Character::isValidCodePoint)
    .filter(FONT::canDisplay)
    .toArray();


  public FontViewerFrame() {
    add(new JScrollPane(new Table()));
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setPreferredSize(new Dimension(800, 600));
    pack();
    setLocationRelativeTo(null);
  }

  private static Font loadFont() {
    try (var is = FontViewerFrame.class.getClassLoader().getResourceAsStream("META-INF/fonts/KaiseiHarunoUmi-Regular.ttf")) {
      return Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(is));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private static final class Table extends JTable {
    private Table() {
      super(new Model());
      createDefaultColumnsFromModel();
      var stringRenderer = new DefaultTableCellRenderer();
      stringRenderer.setFont(FONT);
      setDefaultRenderer(int.class, new DefaultTableCellRenderer());
      setDefaultRenderer(String.class, stringRenderer);
      getSelectionModel().addListSelectionListener(e -> {
        var i = e.getFirstIndex();
        if (i >= 0) {
          var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
          clipboard.setContents(new StringSelection(Character.toString(CPS[i])), null);
        }
      });
    }
  }

  private static final class Model extends AbstractTableModel {

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      return switch (columnIndex) {
        case 0, 1 -> int.class;
        case 2 -> String.class;
        default -> throw new IllegalStateException();
      };
    }

    @Override
    public String getColumnName(int column) {
      return switch (column) {
        case 0 -> "#";
        case 1 -> "CP";
        case 2 -> "Representation";
        default -> throw new IllegalStateException();
      };
    }

    @Override
    public int getRowCount() {
      return CPS.length;
    }

    @Override
    public int getColumnCount() {
      return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      return switch (columnIndex) {
        case 0 -> rowIndex;
        case 1 -> CPS[rowIndex];
        case 2 -> Character.toString(CPS[rowIndex]);
        default -> throw new IllegalStateException();
      };
    }
  }
}
