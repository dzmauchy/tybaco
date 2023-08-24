package org.tybaco.ui.lib.menus;

import org.tybaco.ui.lib.actions.SmartAction;

import javax.swing.*;
import java.util.Map;

public final class Menus {

    private Menus() {
    }

    public static void addMenuItems(JMenu menu, Map<String, SmartAction> map) {
        var grouped = SmartAction.group(map);
        for (var it = grouped.values().iterator(); it.hasNext(); ) {
            var l = it.next();
            l.forEach(menu::add);
            if (it.hasNext()) menu.addSeparator();
        }
    }

    public static void addMenuItems(JPopupMenu menu, Map<String, SmartAction> map) {
        var grouped = SmartAction.group(map);
        for (var it = grouped.values().iterator(); it.hasNext(); ) {
            var l = it.next();
            l.forEach(menu::add);
            if (it.hasNext()) menu.addSeparator();
        }
    }
}
