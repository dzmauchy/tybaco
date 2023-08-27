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

import org.eclipse.aether.RepositoryEvent;
import org.eclipse.aether.RepositoryListener;

@FunctionalInterface
public interface RepoListener extends RepositoryListener {

  default void artifactDescriptorInvalid(RepositoryEvent event) {
    onRepositoryEvent(event);
  }

  default void artifactDescriptorMissing(RepositoryEvent event) {
    onRepositoryEvent(event);
  }

  default void metadataInvalid(RepositoryEvent event) {
    onRepositoryEvent(event);
  }

  default void artifactResolving(RepositoryEvent event) {
    onRepositoryEvent(event);
  }

  default void artifactResolved(RepositoryEvent event) {
    onRepositoryEvent(event);
  }

  default void metadataResolving(RepositoryEvent event) {
    onRepositoryEvent(event);
  }

  default void metadataResolved(RepositoryEvent event) {
    onRepositoryEvent(event);
  }

  default void artifactDownloading(RepositoryEvent event) {
    onRepositoryEvent(event);
  }

  default void artifactDownloaded(RepositoryEvent event) {
    onRepositoryEvent(event);
  }

  default void metadataDownloading(RepositoryEvent event) {
    onRepositoryEvent(event);
  }

  default void metadataDownloaded(RepositoryEvent event) {
    onRepositoryEvent(event);
  }

  default void artifactInstalling(RepositoryEvent event) {
    onRepositoryEvent(event);
  }

  default void artifactInstalled(RepositoryEvent event) {
    onRepositoryEvent(event);
  }

  default void metadataInstalling(RepositoryEvent event) {
    onRepositoryEvent(event);
  }

  default void metadataInstalled(RepositoryEvent event) {
    onRepositoryEvent(event);
  }

  default void artifactDeploying(RepositoryEvent event) {
    onRepositoryEvent(event);
  }

  default void artifactDeployed(RepositoryEvent event) {
    onRepositoryEvent(event);
  }

  default void metadataDeploying(RepositoryEvent event) {
    onRepositoryEvent(event);
  }

  default void metadataDeployed(RepositoryEvent event) {
    onRepositoryEvent(event);
  }

  void onRepositoryEvent(RepositoryEvent event);
}
