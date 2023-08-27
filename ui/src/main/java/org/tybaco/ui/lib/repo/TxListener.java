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

import org.eclipse.aether.transfer.*;

@FunctionalInterface
public interface TxListener extends TransferListener {

  default void transferInitiated(TransferEvent event) {
    onTransferEvent(event);
  }

  default void transferStarted(TransferEvent event) {
    onTransferEvent(event);
  }

  default void transferProgressed(TransferEvent event) {
    onTransferEvent(event);
  }

  default void transferCorrupted(TransferEvent event) {
    onTransferEvent(event);
  }

  default void transferSucceeded(TransferEvent event) {
    onTransferEvent(event);
  }

  default void transferFailed(TransferEvent event) {
    onTransferEvent(event);
  }

  void onTransferEvent(TransferEvent event);
}
