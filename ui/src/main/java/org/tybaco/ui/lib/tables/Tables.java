package org.tybaco.ui.lib.tables;

import javax.swing.*;

public final class Tables {

    private Tables() {
    }

    public static void initColumns(JTable table, int... widths) {
        var cm = table.getColumnModel();
        for (int i = 0; i < widths.length; i++) {
            int w = widths[i];
            var c = cm.getColumn(i);
            c.setPreferredWidth(w);
            c.setMinWidth((w * 10) / 12);
            c.setMaxWidth(w * 4);
        }
    }
}
