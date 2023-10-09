package org.tybaco.editors.java;

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
import com.github.javaparser.serialization.JavaParserJsonDeserializer;
import com.github.javaparser.serialization.JavaParserJsonSerializer;

import javax.json.Json;
import javax.json.stream.JsonGeneratorFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import static javax.json.Json.createGeneratorFactory;
import static javax.json.stream.JsonGenerator.PRETTY_PRINTING;

public final class Expressions {

  private static final JsonGeneratorFactory GENERATOR_FACTORY = createGeneratorFactory(Map.of(PRETTY_PRINTING, true));

  private Expressions() {
  }

  public static String toText(Expression node) {
    var writer = new StringWriter();
    try (var gen = GENERATOR_FACTORY.createGenerator(writer)) {
      var serializer = new JavaParserJsonSerializer();
      serializer.serialize(node, gen);
    }
    return writer.toString();
  }

  public static Expression fromText(String value) {
    var reader = new StringReader(value);
    try (var r = Json.createReader(reader)) {
      var deserializer = new JavaParserJsonDeserializer();
      var node = deserializer.deserializeObject(r);
      return (Expression) node;
    }
  }
}
