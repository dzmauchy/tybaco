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

import org.eclipse.aether.DefaultRepositoryCache;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.collection.*;
import org.eclipse.aether.internal.impl.DefaultRepositorySystem;
import org.eclipse.aether.repository.*;
import org.eclipse.aether.resolution.*;
import org.eclipse.aether.supplier.RepositorySystemSupplier;
import org.eclipse.aether.util.artifact.DefaultArtifactTypeRegistry;
import org.eclipse.aether.util.graph.manager.DefaultDependencyManager;
import org.eclipse.aether.util.graph.transformer.*;
import org.eclipse.aether.util.repository.*;

import java.nio.file.Path;
import java.util.List;

public final class RepositorySystem implements AutoCloseable {

  private final DefaultRepositorySystem system = (DefaultRepositorySystem) new RepositorySystemSupplier().get();

  public RepositorySystem() {
  }

  public ArtifactResult resolve(ArtifactRequest request, DefaultRepositorySystemSession session) throws ArtifactResolutionException {
    return system.resolveArtifact(session, request);
  }

  public List<ArtifactResult> resolve(List<? extends ArtifactRequest> requests, DefaultRepositorySystemSession session) throws ArtifactResolutionException {
    return system.resolveArtifacts(session, requests);
  }

  public DependencyResult resolve(DependencyRequest request, DefaultRepositorySystemSession session) throws DependencyResolutionException {
    return system.resolveDependencies(session, request);
  }

  public CollectResult collect(CollectRequest request, DefaultRepositorySystemSession session) throws DependencyCollectionException {
    return system.collectDependencies(session, request);
  }

  public DefaultRepositorySystemSession session(Path localRepo, TxListener txListener, RepoListener repoListener) {
    var session = new DefaultRepositorySystemSession();
    session.setDependencyGraphTransformer(new ChainedDependencyGraphTransformer(
      new JavaDependencyContextRefiner(),
      new ConflictResolver(
        new NearestVersionSelector(),
        new JavaScopeSelector(),
        new SimpleOptionalitySelector(),
        new JavaScopeDeriver()
      )
    ));
    session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, new LocalRepository(localRepo.toFile())));
    session.setCache(new DefaultRepositoryCache());
    session.setArtifactDescriptorPolicy(new SimpleArtifactDescriptorPolicy(true, true));
    session.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_WARN);
    session.setAuthenticationSelector(new DefaultAuthenticationSelector());
    session.setArtifactTypeRegistry(new DefaultArtifactTypeRegistry());
    session.setMirrorSelector(new DefaultMirrorSelector());
    session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS);
    session.setDependencyManager(new DefaultDependencyManager());
    session.setResolutionErrorPolicy(new SimpleResolutionErrorPolicy(false, false));
    session.setTransferListener(txListener);
    session.setRepositoryListener(repoListener);
    return session;
  }

  public RemoteRepository mavenRepo() {
    return new RemoteRepository.Builder("maven", "default", "https://repo.maven.apache.org/maven2/")
      .build();
  }

  @Override
  public void close() {
    system.shutdown();
  }
}
