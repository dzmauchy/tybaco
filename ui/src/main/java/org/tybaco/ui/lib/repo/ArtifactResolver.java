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


import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.circular.ErrorCircularDependencyStrategy;
import org.apache.ivy.plugins.conflict.LatestConflictManager;
import org.apache.ivy.plugins.latest.LatestRevisionStrategy;
import org.apache.ivy.plugins.lock.NoLockStrategy;
import org.apache.ivy.plugins.resolver.ChainResolver;
import org.apache.ivy.plugins.resolver.IBiblioResolver;
import org.apache.ivy.util.Message;
import org.tybaco.io.CancellablePathCloseable;
import org.tybaco.ui.model.Dependency;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;

import static java.nio.file.Files.isDirectory;

public final class ArtifactResolver {

  static {
    Message.setShowProgress(false);
    Message.setDefaultLogger(new ArtifactMessageLogger());
  }

  public ArtifactClassPath resolve(String name, Iterable<? extends Dependency> deps) throws IOException {
    var ivySettings = new IvySettings();
    var tempDirectory = Files.createTempDirectory("tybaco-repo-");
    var cacheDir = tempDirectory.resolve("cache");
    try (var tempDirWrapper = new CancellablePathCloseable(tempDirectory)) {
      ivySettings.setBaseDir(tempDirectory.toFile());
      ivySettings.setDefaultCache(cacheDir.toFile());
      ivySettings.addResolver(chainResolver(ivySettings));
      ivySettings.setDefaultResolver("public");
      ivySettings.setCheckUpToDate(false);
      ivySettings.setDefaultLockStrategy(new NoLockStrategy());
      ivySettings.setDefaultLatestStrategy(new LatestRevisionStrategy());
      ivySettings.setCircularDependencyStrategy(ErrorCircularDependencyStrategy.getInstance());
      ivySettings.setDefaultConflictManager(new LatestConflictManager(ivySettings.getDefaultLatestStrategy()));

      var ivy = Ivy.newInstance(ivySettings);
      ivy.pushContext();
      try {
        var pmr = ModuleRevisionId.newInstance("org.montoni", "tybaco-project", "working");
        var md = DefaultModuleDescriptor.newDefaultInstance(pmr);
        md.setDefaultConf("default");
        deps.forEach(dep -> {
          var mr = ModuleRevisionId.newInstance(dep.group(), dep.artifact(), dep.version());
          var dd = new DefaultDependencyDescriptor(md, mr, false, false, true);
          dd.addDependencyConfiguration("default", "master");
          dd.addDependencyConfiguration("default", "compile");
          dd.addDependencyConfiguration("default", "runtime");
          md.addDependency(dd);
        });

        var resolveReport = ivy.resolve(md, resolveOptions());
        if (resolveReport.hasError()) {
          var e = new IllegalStateException("Resolve error");
          resolveReport.getAllProblemMessages().forEach(msg -> e.addSuppressed(new IllegalStateException(msg)));
          throw e;
        }

        tempDirWrapper.cancel();
        return new ArtifactClassPath(tempDirectory, name);
      } finally {
        ivy.popContext();
      }
    } catch (ParseException e) {
      throw new IOException(e);
    }
  }

  private ResolveOptions resolveOptions() {
    return new ResolveOptions()
      .setTransitive(true)
      .setDownload(true)
      .setConfs(new String[] {"default"});
  }

  private ChainResolver chainResolver(IvySettings settings) {
    var resolver = new ChainResolver();
    resolver.setSettings(settings);
    resolver.setName("public");
    resolver.setLatestStrategy(new LatestRevisionStrategy());
    resolver.setCheckmodified(false);
    resolver.setReturnFirst(true);
    var localMavenRepoPath = Path.of(System.getProperty("user.home"), ".m2", "repository");
    if (isDirectory(localMavenRepoPath)) {
      var localResolver = new IBiblioResolver();
      localResolver.setRoot(localMavenRepoPath.toUri().toString());
      localResolver.setM2compatible(true);
      localResolver.setUseMavenMetadata(false);
      localResolver.setName("mavenLocal");
      localResolver.setSettings(settings);
      localResolver.setCheckmodified(false);
      localResolver.setLatestStrategy(new LatestRevisionStrategy());
      resolver.add(localResolver);
    }
    var mavenResolver = new IBiblioResolver();
    mavenResolver.setSettings(settings);
    mavenResolver.setUsepoms(true);
    mavenResolver.setM2compatible(true);
    mavenResolver.setUseMavenMetadata(false);
    mavenResolver.setCheckmodified(false);
    mavenResolver.setName("mavenRemote");
    mavenResolver.setLatestStrategy(new LatestRevisionStrategy());
    resolver.add(mavenResolver);
    return resolver;
  }
}
