package org.tybaco.annotations.sink;

/*-
 * #%L
 * annotations
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

import org.tybaco.annotations.AbstractFileProcessor;
import org.tybaco.annotations.ProcessedFile;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class SinkAnnotationProcessor extends AbstractFileProcessor {

  @Override
  protected String annotation() {
    return "org.tybaco.runtime.annotations.Sink";
  }

  @Override
  protected List<ProcessedFile> process(String code, String name) {
    return Stream.of("Double", "Long", "Int")
      .map(t -> {
        var newName = name.substring(0, name.length() - 4) + t + "Sink";
        var c = code
          .replace("Sink<E>", t + "Sink")
          .replace("Source<E>", "org.tybaco.runtime.basic.source." + t + "Source")
          .replace("Consumer<? super E>", Consumer.class.getPackageName() + "." + t + "Consumer")
          .replace("public " + name + "(", "public " + newName + "(")
          .replace("@Sink", "");
        return new ProcessedFile(newName, c);
      })
      .toList();
  }
}
