package org.tybaco.ui.child.project.classpath;

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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tybaco.meta.Library;
import org.tybaco.xml.Xml;

import java.util.stream.Stream;

import static org.tybaco.logging.Log.warn;
import static org.tybaco.xml.Xml.schema;

@Component
public final class LibraryFinder {

  private final ProjectClasspath classpath;
  private final String librarySchemaPath;

  public LibraryFinder(ProjectClasspath classpath, @Value("${tybaco.library.schema}") String schemaPath) {
    this.classpath = classpath;
    this.librarySchemaPath = schemaPath;
  }

  public Stream<Library> libraries() {
    var cp = classpath.classPath.get();
    if (cp == null || cp.classLoader.getURLs().length == 0) {
      return Stream.empty();
    }
    var schema = schema(cp.classLoader.getResource(librarySchemaPath));
    return cp.classLoader.resources("tybaco/library/library.xml")
      .flatMap(url -> {
        try {
          var lib = Xml.loadFrom(url, schema, Library::new);
          return Stream.of(lib);
        } catch (Throwable e) {
          warn(getClass(), "Unable to load library {0}", e, url);
          return Stream.empty();
        }
      });
  }
}
