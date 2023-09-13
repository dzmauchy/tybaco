package org.tybaco.ui.lib.repo;

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

import org.apache.ivy.util.AbstractMessageLogger;

import static org.apache.ivy.util.Message.*;
import static org.tybaco.ui.lib.logging.Logging.LOG;

public final class ArtifactMessageLogger extends AbstractMessageLogger {

  @Override
  protected void doProgress() {
  }

  @Override
  protected void doEndProgress(String msg) {
  }

  @Override
  public void log(String msg, int level) {
    switch (level) {
      case MSG_INFO -> LOG.info(msg);
      case MSG_VERBOSE -> LOG.fine(msg);
      case MSG_DEBUG -> LOG.finer(msg);
      case MSG_WARN -> LOG.warning(msg);
      case MSG_ERR -> LOG.severe(msg);
    }
  }

  @Override
  public void rawlog(String msg, int level) {
    switch (level) {
      case MSG_INFO -> LOG.info(msg);
      case MSG_VERBOSE -> LOG.fine(msg);
      case MSG_DEBUG -> LOG.finer(msg);
      case MSG_WARN -> LOG.warning(msg);
      case MSG_ERR -> LOG.severe(msg);
    }
  }
}
