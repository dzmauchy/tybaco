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
import org.apache.ivy.plugins.resolver.IBiblioResolver;
import org.apache.ivy.util.Message;
import org.tybaco.io.CancellablePathCloseable;
import org.tybaco.io.PathCloseable;
import org.tybaco.ui.model.Lib;

import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.List;

public final class ArtifactResolver {

  static {
    Message.setShowProgress(false);
    Message.setDefaultLogger(new ArtifactMessageLogger());
  }

  public ArtifactClassPath resolve(String name, List<Lib> libs) throws IOException {
    var ivySettings = new IvySettings();
    var tempDirectory = Files.createTempDirectory("tybaco-repo-");
    var cacheDir = tempDirectory.resolve("cache");
    try (var tempDirWrapper = new CancellablePathCloseable(tempDirectory)) {
      ivySettings.setBaseDir(tempDirectory.toFile());
      ivySettings.setDefaultCache(cacheDir.toFile());
      ivySettings.addResolver(mavenResolver(ivySettings));
      ivySettings.setDefaultResolver("public");

      var ivy = Ivy.newInstance(ivySettings);
      ivy.pushContext();
      try {
        var pmr = ModuleRevisionId.newInstance("org.montoni", "tybaco-project", "working");
        var md = DefaultModuleDescriptor.newDefaultInstance(pmr);
        md.setDefaultConf("default");
        for (var lib : libs) {
          var mr = ModuleRevisionId.newInstance(lib.group(), lib.artifact(), lib.version());
          var dd = new DefaultDependencyDescriptor(md, mr, false, false, true);
          dd.addDependencyConfiguration("default", "master");
          dd.addDependencyConfiguration("default", "compile");
          dd.addDependencyConfiguration("default", "runtime");
          md.addDependency(dd);
        }

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

  private IBiblioResolver mavenResolver(IvySettings settings) {
    var resolver = new IBiblioResolver();
    resolver.setSettings(settings);
    resolver.setUsepoms(true);
    resolver.setM2compatible(true);
    resolver.setUseMavenMetadata(true);
    resolver.setName("public");
    return resolver;
  }
}
