package org.tybloco.xml;

/*-
 * #%L
 * library
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.IntStream.range;
import static javax.xml.transform.OutputKeys.*;

public class Xml {

  public static Stream<Element> elementsByTag(Element element, String tag) {
    var list = element.getElementsByTagName(tag);
    return range(0, list.getLength()).mapToObj(list::item).map(Element.class::cast);
  }

  public static Optional<Element> elementByTag(Element element, String tag) {
    return elementsByTag(element, tag).findFirst();
  }

  public static void withChild(Element element, String tag, Consumer<Element> consumer) {
    var child = element.getOwnerDocument().createElement(tag);
    element.appendChild(child);
    consumer.accept(child);
  }

  public static <T> void withChildren(Element element, String tag, Iterable<T> list, BiConsumer<T, Element> consumer) {
    list.forEach(e -> withChild(element, tag, elem -> consumer.accept(e, elem)));
  }

  public static <T> T loadFrom(Path path, Schema schema, Function<Element, T> func) {
    try (var reader = Files.newBufferedReader(path, UTF_8)) {
      return loadFrom(new InputSource(reader), schema, func);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static <T> T loadFrom(File file, Schema schema, Function<Element, T> func) {
    var inputSource = new InputSource();
    inputSource.setEncoding("UTF-8");
    inputSource.setSystemId(file.toURI().toString());
    inputSource.setPublicId(file.getName());
    return loadFrom(inputSource, schema, func);
  }

  public static <T> T loadFrom(URL url, Schema schema, Function<Element, T> func) {
    try (var inputStream = url.openStream()) {
      var inputSource = new InputSource(inputStream);
      inputSource.setEncoding("UTF-8");
      inputSource.setSystemId(url.toExternalForm());
      return loadFrom(inputSource, schema, func);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static <T> T loadFrom(InputSource inputSource, Schema schema, Function<Element, T> func) {
    var dbf = DocumentBuilderFactory.newDefaultInstance();
    dbf.setIgnoringComments(true);
    dbf.setIgnoringElementContentWhitespace(true);
    dbf.setCoalescing(true);
    dbf.setSchema(schema);
    try {
      var db = dbf.newDocumentBuilder();
      db.setErrorHandler(new XmlErrorHandler());
      var doc = db.parse(inputSource);
      var element = doc.getDocumentElement();
      return func.apply(element);
    } catch (ParserConfigurationException | SAXException e) {
      throw new IllegalStateException(e);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static void saveTo(Path path, String tag, Consumer<Element> consumer) {
    try (var writer = Files.newBufferedWriter(path, UTF_8)) {
      saveTo(new StreamResult(writer), tag, consumer);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static void saveTo(File file, String tag, Consumer<Element> consumer) {
    saveTo(new StreamResult(file), tag, consumer);
  }

  public static void saveTo(Result result, String tag, Consumer<Element> consumer) {
    var doc = newDocument();
    var docElement = doc.createElement(tag);
    doc.appendChild(docElement);
    consumer.accept(docElement);
    var t = newTransformer();
    t.setOutputProperty(INDENT, "yes");
    t.setOutputProperty(ENCODING, "UTF-8");
    t.setOutputProperty(STANDALONE, "yes");
    t.setOutputProperty(OMIT_XML_DECLARATION, "no");
    try {
      t.transform(new DOMSource(doc), result);
    } catch (TransformerException e) {
      throw new IllegalStateException(e); // almost impossible
    }
  }

  public static Schema schema(String resource) {
    var classLoader = Thread.currentThread().getContextClassLoader();
    var url = classLoader.getResource(resource);
    return schema(url);
  }

  public static Schema schema(URL url) {
    var schemaFactory = SchemaFactory.newDefaultInstance();
    try {
      return schemaFactory.newSchema(url);
    } catch (SAXException e) {
      throw new IllegalStateException(e); // should be tested
    }
  }

  private static Document newDocument() {
    var dbf = DocumentBuilderFactory.newDefaultInstance();
    try {
      var db = dbf.newDocumentBuilder();
      return db.newDocument();
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException(e); // impossible
    }
  }

  private static Transformer newTransformer() {
    var tf = TransformerFactory.newDefaultInstance();
    tf.setAttribute("indent-number", 2);
    try {
      return tf.newTransformer();
    } catch (TransformerConfigurationException e) {
      throw new IllegalStateException(e); // impossible
    }
  }
}
