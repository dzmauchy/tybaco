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

import lombok.extern.java.Log;
import org.eclipse.aether.RepositoryEvent;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.util.filter.ScopeDependencyFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.logging.Level.INFO;
import static org.apache.maven.artifact.Artifact.SCOPE_COMPILE_PLUS_RUNTIME;
import static org.apache.maven.artifact.Artifact.SCOPE_RUNTIME;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.io.CleanupMode.ALWAYS;

@Log
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class RepositorySystemTest {

  @TempDir(cleanup = ALWAYS)
  private Path tempDir;

  @Test
  void resolveArtifact() throws Exception {
    try (var system = new RepositorySystem()) {
      var artifact = new DefaultArtifact("org.slf4j", "jcl-over-slf4j", "jar", "2.0.7");
      var req = new ArtifactRequest(artifact, List.of(system.mavenRepo()), null);
      var txEvents = new ConcurrentLinkedQueue<TransferEvent>();
      var repoEvents = new ConcurrentLinkedQueue<RepositoryEvent>();
      var session = system.session(tempDir, txEvents::add, repoEvents::add);
      var result = system.resolve(req, session);
      txEvents.forEach(e -> log.log(INFO, "{0}", e));
      repoEvents.forEach(e -> log.log(INFO, "{0}", e));
      assertTrue(result.isResolved());
      assertFalse(result.isMissing());
      assertTrue(result.getArtifact().getFile().length() > 0L);
      assertFalse(txEvents.isEmpty());
      assertFalse(repoEvents.isEmpty());
    }
  }

  @Test
  void collectDependency() throws Exception {
    try (var system = new RepositorySystem()) {
      var artifact = new DefaultArtifact("org.slf4j", "jcl-over-slf4j", "jar", "2.0.7");
      var request = new CollectRequest(new Dependency(artifact, SCOPE_COMPILE_PLUS_RUNTIME), List.of(system.mavenRepo()));
      var txEvents = new ConcurrentLinkedQueue<TransferEvent>();
      var repoEvents = new ConcurrentLinkedQueue<RepositoryEvent>();
      var session = system.session(tempDir, txEvents::add, repoEvents::add);
      var result = system.collect(request, session);
      txEvents.forEach(e -> log.log(INFO, "{0}", e));
      repoEvents.forEach(e -> log.log(INFO, "{0}", e));
      assertFalse(txEvents.isEmpty());
      assertFalse(repoEvents.isEmpty());
      assertNotNull(result.getRoot());
      assertFalse(result.getRoot().getChildren().isEmpty());
    }
  }

  @Test
  void resolveDependency() throws Exception {
    try (var system = new RepositorySystem()) {
      var artifact = new DefaultArtifact("org.slf4j", "jcl-over-slf4j", "jar", "2.0.7");
      var collectRequest = new CollectRequest(new Dependency(artifact, SCOPE_RUNTIME), List.of(system.mavenRepo()));
      var request = new DependencyRequest(collectRequest, new ScopeDependencyFilter(Set.of("runtime", "compile"), Set.of()));
      var txEvents = new ConcurrentLinkedQueue<TransferEvent>();
      var repoEvents = new ConcurrentLinkedQueue<RepositoryEvent>();
      var session = system.session(tempDir, txEvents::add, repoEvents::add);
      var result = system.resolve(request, session);
      txEvents.forEach(e -> log.log(INFO, "{0}", e));
      repoEvents.forEach(e -> log.log(INFO, "{0}", e));
      assertFalse(txEvents.isEmpty());
      assertFalse(repoEvents.isEmpty());
      assertNotNull(result.getRoot());
      assertEquals(2, result.getArtifactResults().size());
      assertFalse(result.getRoot().getChildren().isEmpty());
      for (var r : result.getArtifactResults()) {
        assertTrue(r.isResolved());
        assertFalse(r.isMissing());
        assertNotNull(r.getArtifact().getFile());
        assertTrue(r.getArtifact().getFile().exists());
        assertTrue(r.getArtifact().getFile().toPath().startsWith(tempDir));
      }
    }
  }
}
