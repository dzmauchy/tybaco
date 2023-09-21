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

import java.nio.file.Path;
import java.util.ArrayList;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readString;
import static java.nio.file.Files.writeString;

public class SortL10n {

  public static void main(String... args) throws Exception {
    for (var arg : args) {
      System.out.printf("Sorting %s%n", arg);
      var path = Path.of(arg);
      var str = readString(path, UTF_8);
      var recs = new ArrayList<Rec>();
      for (int i = str.indexOf("<key value=\""); i >= 0; ) {
        var j = str.indexOf("</key>", i) + 6;
        recs.add(new Rec(i, j, str.substring(i, j)));
        i = str.indexOf("<key value=\"", j);
      }
      var sortedRecs = recs.stream().sorted().toList();
      var builder = new StringBuilder(str);
      for (int i = recs.size() - 1; i >= 0; i--) {
        var rec = recs.get(i);
        var sortedRec = sortedRecs.get(i);
        builder.replace(rec.start, rec.end, sortedRec.value);
      }
      writeString(path, builder, UTF_8);
    }
  }

  private record Rec(int start, int end, String value) implements Comparable<Rec> {
    @Override
    public int compareTo(SortL10n.Rec o) {
      return value.compareTo(o.value);
    }
  }
}
