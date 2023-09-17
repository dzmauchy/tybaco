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
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.*;
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

  static <T> T load(URL url, Function<Element, T> supplier) {
    try {
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
        return load(inputSource, supplier);
      } finally {
        if (connection instanceof HttpURLConnection c) {
          c.disconnect();
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  static <T> T load(InputSource inputSource, Function<Element, T> supplier) {
    try {
      var documentBuilderFactory = DocumentBuilderFactory.newDefaultInstance();
      var documentBuilder = documentBuilderFactory.newDocumentBuilder();
      var document = documentBuilder.parse(inputSource);
      return supplier.apply(document.getDocumentElement());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (ParserConfigurationException | SAXException e) {
      throw new IllegalStateException(e);
    }
  }
}
