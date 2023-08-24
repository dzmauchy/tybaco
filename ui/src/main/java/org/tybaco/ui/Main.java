package org.tybaco.ui;

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

import com.formdev.flatlaf.FlatDarculaLaf;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.tybaco.logging.FastConsoleHandler;
import org.tybaco.logging.LoggingManager;
import org.tybaco.ui.lib.logging.UILogHandler;
import org.tybaco.ui.main.MainConfiguration;
import org.tybaco.ui.main.MainFrame;

import javax.swing.*;
import java.util.concurrent.CountDownLatch;

import static java.awt.EventQueue.invokeLater;
import static java.lang.System.setProperty;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.INFO;
import static java.util.logging.Logger.getLogger;

public final class Main implements ApplicationListener<ApplicationEvent> {

    public static void main(String... args) {
        initLogging();
        var ctx = new AnnotationConfigApplicationContext();
        var latch = new CountDownLatch(1);
        invokeLater(() -> bootstrap(latch, ctx));
        try {
            ctx.setId("root");
            ctx.setDisplayName("TybacoIDE");
            ctx.setClassLoader(Thread.currentThread().getContextClassLoader());
            ctx.setAllowCircularReferences(false);
            ctx.setAllowBeanDefinitionOverriding(false);
            ctx.addApplicationListener(new Main());
            ctx.register(MainConfiguration.class);
            ctx.refresh();
        } finally {
            latch.countDown();
        }
    }

    @SneakyThrows
    private static void bootstrap(CountDownLatch latch, GenericApplicationContext context) {
        UIManager.setLookAndFeel(new FlatDarculaLaf());
        latch.await();
        var mainFrame = requireNonNull(context.getBean(MainFrame.class), "No MainFrame found");
        mainFrame.setVisible(true);
    }

    private static void initLogging() {
        setProperty("java.util.logging.manager", LoggingManager.class.getName());
        var rootLogger = getLogger("");
        rootLogger.addHandler(new FastConsoleHandler());
        rootLogger.addHandler(new UILogHandler());
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        getLogger("").log(INFO, "{0}", event);
    }
}
