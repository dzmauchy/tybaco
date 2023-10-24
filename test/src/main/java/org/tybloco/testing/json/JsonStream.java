package org.tybloco.testing.json;

/*-
 * #%L
 * test
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

import com.google.gson.*;

import java.io.*;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Spliterators.spliteratorUnknownSize;

public final class JsonStream {

  private final JsonStreamParser streamParser;

  private JsonStream(JsonStreamParser streamParser) {
    this.streamParser = streamParser;
  }

  public JsonStream(ByteArrayOutputStream bos) {
    this(new JsonStreamParser(new StringReader(bos.toString(UTF_8))));
  }

  public JsonStream(CharSequence sequence) {
    this(new JsonStreamParser(new StringReader(sequence.toString())));
  }

  public static List<JsonElement> elementList(ByteArrayOutputStream bos) {
    return new JsonStream(bos).stream().toList();
  }

  public static List<JsonElement> elementList(CharSequence sequence) {
    return new JsonStream(sequence).stream().toList();
  }

  public static List<JsonObject> objectList(ByteArrayOutputStream bos) {
    return new JsonStream(bos).stream()
      .filter(JsonObject.class::isInstance)
      .map(JsonObject.class::cast)
      .toList();
  }

  public static List<JsonObject> objectList(CharSequence sequence) {
    return new JsonStream(sequence).stream()
      .filter(JsonObject.class::isInstance)
      .map(JsonObject.class::cast)
      .toList();
  }

  public Stream<JsonElement> stream() {
    var spliterator = spliteratorUnknownSize(streamParser, Spliterator.NONNULL | Spliterator.ORDERED);
    return StreamSupport.stream(spliterator, false);
  }

  public Stream<JsonObject> objectStream() {
    return stream().filter(JsonObject.class::isInstance).map(JsonObject.class::cast);
  }
}
