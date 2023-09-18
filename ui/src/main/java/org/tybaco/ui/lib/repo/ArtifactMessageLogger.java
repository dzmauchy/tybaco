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
import org.tybaco.logging.Log;

import static java.util.logging.Level.*;
import static org.apache.ivy.util.Message.*;

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
      case MSG_INFO -> Log.log(getClass(), INFO, msg);
      case MSG_VERBOSE -> Log.log(getClass(), FINE, msg);
      case MSG_DEBUG -> Log.log(getClass(), FINER, msg);
      case MSG_WARN -> Log.log(getClass(), WARNING, msg);
      case MSG_ERR -> Log.log(getClass(), SEVERE, msg);
    }
  }

  @Override
  public void rawlog(String msg, int level) {
    log(msg, level);
  }
}
