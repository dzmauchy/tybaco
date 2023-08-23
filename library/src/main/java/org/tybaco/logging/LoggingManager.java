package org.tybaco.logging;

/*-
 * #%L
 * library
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.LogManager;

import static java.util.Arrays.stream;

public final class LoggingManager extends LogManager {

    private final AtomicBoolean first = new AtomicBoolean(true);

    @Override
    public void reset() throws SecurityException {
        if (first.compareAndSet(true, false)) {
            super.reset();
        }
    }

    @Override
    public void readConfiguration() throws IOException, SecurityException {
        super.readConfiguration();
    }

    @Override
    public void readConfiguration(InputStream ins) throws IOException, SecurityException {
        super.readConfiguration(ins);
        var logger = getLogger("");
        var cl = Thread.currentThread().getContextClassLoader();
        var handlers = stream(Objects.requireNonNullElse(getProperty(".logHandlers"), "").split(","))
                .filter(s -> !s.isBlank())
                .map(String::trim)
                .toArray(String[]::new);
        for (var handler : handlers) {
            try {
                var cls = cl.loadClass(handler);
                for (var c : cls.getConstructors()) {
                    if (c.getParameterCount() == 0) {
                        var h = (Handler) c.newInstance();
                        logger.addHandler(h);
                        break;
                    }
                }
            } catch (ReflectiveOperationException e) {
                throw new InvalidClassException(handler, "not found", e);
            }
        }
    }
}