package org.tybaco.ui.child.logging;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.stream.IntStream;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static org.tybaco.ui.lib.images.ImageCache.svgImage;

@Component
public class LogFrame extends JFrame {

    private final GenericApplicationContext context;

    public LogFrame(GenericApplicationContext context) {
        super("Log");
        this.context = context;
        setIconImages(IntStream.of(18, 24).mapToObj(size -> svgImage("icon/logs.svg", size)).toList());
        setType(Type.UTILITY);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(1024, 768));
    }

    @Autowired
    public void withLogTable(LogTable table) {
        var sp = new JScrollPane(table, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(sp, BorderLayout.CENTER);
    }

    @EventListener
    public void onStart(ContextRefreshedEvent event) {
        pack();
        setLocationRelativeTo(null);
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        switch (e.getID()) {
            case WindowEvent.WINDOW_OPENED -> context.start();
            case WindowEvent.WINDOW_CLOSING -> context.stop();
            case WindowEvent.WINDOW_CLOSED -> context.close();
        }
    }
}
