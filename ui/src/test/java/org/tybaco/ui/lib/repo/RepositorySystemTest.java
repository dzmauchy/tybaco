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

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.tybaco.ui.model.Lib;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("slow")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Execution(ExecutionMode.CONCURRENT)
class RepositorySystemTest {

  private final ArtifactResolver resolver = new ArtifactResolver();

  @Test
  void resolveArtifacts() throws Exception {
    try (var cp = resolver.resolve("test", List.of(new Lib("org.slf4j", "slf4j-jdk14", "2.0.9")))) {
      var classLoader = cp.getClassLoader();
      assertEquals(2, classLoader.getURLs().length);
    }
  }
}
