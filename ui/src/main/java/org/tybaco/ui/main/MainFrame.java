package org.tybaco.ui.main;

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

import lombok.extern.java.Log;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

@Component
@Log
public final class MainFrame extends JFrame {

    private final GenericApplicationContext context;

    public MainFrame(GenericApplicationContext context) {
        super("Tybaco IDE");
        this.context = context;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(new JPanel());
        setName("mainFrame");
        setPreferredSize(new Dimension(800, 600));
        pack();
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        log.info("MainFrame created");
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
