package org.tybaco.xml;

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

import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.IntStream.range;

public class Xml {

  public static Stream<Element> elementsByTag(Element element, String tag) {
    var list = element.getElementsByTagName(tag);
    return range(0, list.getLength())
      .mapToObj(list::item)
      .filter(Element.class::isInstance)
      .map(Element.class::cast);
  }

  public static Element elementByTag(Element element, String tag) {
    var list = element.getElementsByTagName(tag);
    return (Element) list.item(0);
  }

  public static Stream<Element> elementsByTags(Element element, String enclosingTag, String tag) {
    return elementsByTag(element, enclosingTag).flatMap(e -> elementsByTag(e, tag));
  }

  public static void withChild(Element element, String tag, Consumer<Element> consumer) {
    var doc = element.getOwnerDocument();
    var child = doc.createElement(tag);
    element.appendChild(child);
    consumer.accept(child);
  }

  public static <T> T loadFrom(Path path, Function<Element, T> func) {
    try (var reader = Files.newBufferedReader(path, UTF_8)) {
      return loadFrom(new InputSource(reader), func);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static <T> T loadFrom(URL url, Function<Element, T> func) {
    try (var inputStream = url.openStream()) {
      var inputSource = new InputSource(inputStream);
      inputSource.setEncoding("UTF-8");
      return loadFrom(inputSource, func);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static <T> T loadFrom(InputSource inputSource, Function<Element, T> func) {
    var dbf = DocumentBuilderFactory.newDefaultInstance();
    dbf.setIgnoringComments(true);
    dbf.setIgnoringElementContentWhitespace(true);
    dbf.setCoalescing(true);
    try {
      var db = dbf.newDocumentBuilder();
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

  public static void saveTo(Result result, String tag, Consumer<Element> consumer) {
    var dbf = DocumentBuilderFactory.newDefaultInstance();
    var tf = TransformerFactory.newDefaultInstance();
    tf.setAttribute("indent-number", 2);
    try {
      var db = dbf.newDocumentBuilder();
      var doc = db.newDocument();
      var docElement = doc.createElement(tag);
      doc.appendChild(docElement);
      consumer.accept(docElement);
      var t = tf.newTransformer();
      t.setOutputProperty(OutputKeys.INDENT, "yes");
      t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      t.setOutputProperty(OutputKeys.STANDALONE, "yes");
      t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");;
      t.transform(new DOMSource(doc), result);
    } catch (TransformerException e) {
      if (e.getCause() instanceof IOException x) {
        throw new UncheckedIOException(x);
      } else {
        throw new IllegalStateException(e);
      }
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException(e);
    }
  }
}
