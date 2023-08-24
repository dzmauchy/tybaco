package org.tybaco.ui.lib.actions;

import org.tybaco.ui.lib.images.ImageCache;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toUnmodifiableList;

public final class SmartAction extends AbstractAction {

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

    public static TreeMap<String, List<SmartAction>> group(Map<String, SmartAction> actions) {
        return actions.entrySet().stream().collect(groupingBy(
                e -> extractGroupFromName(e.getKey()),
                TreeMap::new,
                mapping(Entry::getValue, toUnmodifiableList())
        ));
    }

    private static String extractGroupFromName(String name) {
        var idx = name.indexOf('_');
        return idx < 0 ? "" : name.substring(0, idx);
    }
}
