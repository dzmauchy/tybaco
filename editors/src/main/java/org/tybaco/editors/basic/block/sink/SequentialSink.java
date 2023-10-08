package org.tybaco.editors.basic.block.sink;

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

import com.github.javaparser.ast.expr.Expression;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tybaco.editors.model.*;
import org.tybaco.editors.util.SeqMap;

import java.util.List;
import java.util.Map;

import static org.tybaco.editors.model.LibInput.optional;
import static org.tybaco.editors.model.LibInput.required;

@Qualifier("basic")
@Component
@Descriptor(id = "SeqSink", name = "Sink of values", icon = "mdal-confirmation_number", description = "Simple sequential sink of values")
public final class SequentialSink implements LibBlock {

  @Override
  public SeqMap<String, LibInput> inputs() {
    return new SeqMap<>(
      "tf", optional("Thread factory", "ri-5star-shadow", "A thread factory used to create the main thread of the sink", "$defaultThreadFactory"),
      "source", required("Source", "ri-dharma-wheel", "A key-value source"),
      "consumer", required("Consumer", "ri-react", "A consumer to consume the each key-value pair of the source"),
      "onError", optional("Error handler", "ri-sdg", "Error handler used to handle each error", "$defaultErrorHandler")
    );
  }

  @Override
  public SeqMap<String, LibOutput> outputs() {
    return new SeqMap<>(
      "self", new LibOutput("Self", "mdal-alternate_email", "A resulting sink")
    );
  }

  @Override
  public BlockResult build(String var, Map<String, List<Expression>> inputs) {
    return null;
  }
}
