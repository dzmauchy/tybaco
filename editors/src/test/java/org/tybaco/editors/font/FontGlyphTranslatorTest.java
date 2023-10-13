package org.tybaco.editors.font;

/*-
 * #%L
 * editors
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

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.IntStream;

@Tag("manual")
public class FontGlyphTranslatorTest {

  @Test
  public void test() throws Exception {
    var dir = Path.of(System.getProperty("user.home"), "tyfonts");
    Files.createDirectories(dir);
    for (int i = 499, p = 0; i < FontViewerFrame.CPS.length; i += 2000, p++) {
      var l = Math.min(FontViewerFrame.CPS.length, i + 2000);
      var file = dir.resolve("page-" + p + ".txt");
      var lines = IntStream.range(i, l).mapToObj(e -> Character.toString(FontViewerFrame.CPS[e])).toList();
      Files.write(file, lines, StandardCharsets.UTF_8);
    }
  }

  @Test
  public void form() throws Exception {
    var dir = Path.of(System.getProperty("user.home"), "tyfonts");
    if (!Files.isDirectory(dir)) {
      return;
    }
    var mapping = new ArrayList<String>();
    try (var ds = Files.newDirectoryStream(dir, "page-?.txt")) {
      for (var f : ds) {
        var name = f.getFileName().toString();
        var newName = name.substring(0, name.length() - 4) + "t.txt";
        var l1 = Files.readAllLines(f, StandardCharsets.UTF_8);
        var l2 = Files.readAllLines(dir.resolve(newName), StandardCharsets.UTF_8);
        for (int i = 0; i < l1.size(); i++) {
          mapping.add(l1.get(i) + "=" + l2.get(i));
        }
      }
    }
    Files.write(dir.resolve("mapping.txt"), mapping, StandardCharsets.UTF_8);
  }
}
