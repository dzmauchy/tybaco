package org.tybaco.runtime.util;

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

import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface Xml {

  static Stream<Element> elementsByTag(Element element, String tag) {
    var children = element.getChildNodes();
    return IntStream.range(0, children.getLength())
      .mapToObj(children::item)
      .filter(Element.class::isInstance)
      .map(Element.class::cast)
      .filter(e -> tag.equals(e.getTagName()));
  }

  static Optional<Element> elementByTag(Element element, String tag) {
    return elementsByTag(element, tag).findFirst();
  }

  static <T> T load(URL url, Schema schema, Function<Element, T> supplier) throws SAXException, IOException {
    var connection = url.openConnection();
    if (connection instanceof HttpURLConnection c) {
      c.setUseCaches(false);
      c.setReadTimeout(10_000);
      c.setConnectTimeout(10_000);
    }
    connection.connect();
    try (var is = connection.getInputStream()) {
      var inputSource = new InputSource(is);
      inputSource.setEncoding("UTF-8");
      inputSource.setSystemId(url.toExternalForm());
      return load(inputSource, schema, supplier);
    } finally {
      if (connection instanceof HttpURLConnection c) {
        c.disconnect();
      }
    }
  }

  static <T> T load(InputSource inputSource, Schema schema, Function<Element, T> supplier) throws SAXException, IOException {
    try {
      var documentBuilderFactory = DocumentBuilderFactory.newDefaultInstance();
      documentBuilderFactory.setSchema(schema);
      var documentBuilder = documentBuilderFactory.newDocumentBuilder();
      documentBuilder.setErrorHandler(new XmlErrorHandler());
      var document = documentBuilder.parse(inputSource);
      return supplier.apply(document.getDocumentElement());
    } catch (ParserConfigurationException e) {
      throw new IOException(e);
    }
  }

  static Schema loadSchema(URL url) throws SAXException {
    var schemaFactory = SchemaFactory.newDefaultInstance();
    return schemaFactory.newSchema(url);
  }

  static Schema loadSchema(String resource) throws SAXException {
    return loadSchema(Thread.currentThread().getContextClassLoader().getResource(resource));
  }
}
