package org.tybaco.runtime.basic.error;

/*-
 * #%L
 * runtime
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

import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public interface ErrorConsumers {

  static Consumer<? super Throwable> logErrorConsumer(String loggerName, String message) {
    var logger = LoggerFactory.getLogger(loggerName);
    return e -> {
      switch (e) {
        case null -> logger.error(message);
        case RuntimeException x -> logger.error("Runtime error: {}", message, x);
        case Exception x -> logger.error("Exception: {}", message, x);
        case Throwable x -> logger.error("Error: {}", message, x);
      }
    };
  }
}
