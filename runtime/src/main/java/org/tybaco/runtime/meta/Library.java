package org.tybaco.runtime.meta;

/*-
 * #%L
 * runtime
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

import org.tybaco.runtime.util.Xml;
import org.w3c.dom.Element;

import java.net.URL;
import java.util.List;

import static org.tybaco.runtime.util.Xml.elementsByTag;

public record Library(Meta meta, List<LibraryBlocks> blocks, List<LibraryConstants> constants, List<LibraryType> types) {

  public Library(Element element) {
    this(
      new Meta(element),
      elementsByTag(element, "blocks").map(LibraryBlocks::new).toList(),
      elementsByTag(element, "constants").map(LibraryConstants::new).toList(),
      elementsByTag(element, "type").map(LibraryType::new).toList()
    );
  }

  public static Library load(URL url) {
    return Xml.load(url, Library::new);
  }
}
