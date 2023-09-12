package org.tybaco.runtime.application;

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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.*;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class ApplicationLoader implements Runnable {

  private final String[] args;

  public ApplicationLoader(String[] args) {
    this.args = args;
  }

  @Override
  public void run() {
    var documentBuilderFactory = DocumentBuilderFactory.newDefaultInstance();
    final Document document;
    try {
      var documentBuilder = documentBuilderFactory.newDocumentBuilder();
      if (args.length > 0) {
        var url = new URI(args[0]).toURL();
        var connection = url.openConnection();
        if (connection instanceof HttpURLConnection c) {
          c.setReadTimeout(60_000);
          c.setConnectTimeout(60_000);
          c.setInstanceFollowRedirects(true);
          c.setUseCaches(false);
          c.setAllowUserInteraction(false);
        }
        connection.connect();
        try (var inputStream = connection.getInputStream()) {
          var inputSource = new InputSource(inputStream);
          inputSource.setEncoding("UTF-8");
          inputSource.setSystemId(url.toExternalForm());
          document = documentBuilder.parse(inputSource);
        } finally {
          if (connection instanceof HttpURLConnection c) {
            c.disconnect();
          }
        }
      } else {
        var inputSource = new InputSource(System.in);
        inputSource.setEncoding("UTF-8");
        inputSource.setPublicId("stdin");
        document = documentBuilder.parse(inputSource);
      }
      application(document);
    } catch (URISyntaxException | MalformedURLException e) {
      throw new IllegalStateException("Invalid URL: " + Arrays.asList(args));
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException(e);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (SAXException e) {
      throw new IllegalStateException("XML format error", e);
    } catch (RuntimeException e) {
      throw new IllegalStateException("Parse error", e);
    }
  }

  private void application(Document document) {
    var root = requireNonNull(document.getDocumentElement(), "Document element is null");
    var application = new Application(
      requireNonNull(root.getAttribute("id"), "id attribute is null"),
      constants(root),
      blocks(root),
      links(root)
    );
    Application.CURRENT_APPLICATION.set(application);
  }

  private List<ApplicationConstant> constants(Element element) {
    var list = new LinkedList<ApplicationConstant>();
    var wrapperNodes = element.getElementsByTagName("constants");
    for (int i = 0; i < wrapperNodes.getLength(); i++) {
      if (wrapperNodes.item(i) instanceof Element blocks) {
        var nodes = blocks.getElementsByTagName("constant");
        for (int j = 0; j < nodes.getLength(); j++) {
          if (nodes.item(j) instanceof Element block) {
            list.add(constant(block, i, j));
          }
        }
      }
    }
    return List.copyOf(list);
  }

  private ApplicationConstant constant(Element element, int i, int j) {
    var id = parseInt(element.getAttribute("id")).orElseThrow(() -> new IllegalArgumentException("Invalid block [%d][%d]".formatted(i, j)));
    var factory = requireNonNull(element.getAttribute("factory"), () -> "Constant factory is null: %d".formatted(id));
    if (factory.isBlank()) {
      throw new IllegalArgumentException("Block factory is blank: %d".formatted(id));
    }
    var value = requireNonNull(element.getAttribute("value"), () -> "Block value is null [%d][%d]".formatted(i, j));
    return new ApplicationConstant(id, factory, value);

  }

  private List<ApplicationBlock> blocks(Element element) {
    var list = new LinkedList<ApplicationBlock>();
    var wrapperNodes = element.getElementsByTagName("blocks");
    for (int i = 0; i < wrapperNodes.getLength(); i++) {
      if (wrapperNodes.item(i) instanceof Element blocks) {
        var nodes = blocks.getElementsByTagName("block");
        for (int j = 0; j < nodes.getLength(); j++) {
          if (nodes.item(j) instanceof Element block) {
            list.add(block(block, i, j));
          }
        }
      }
    }
    return List.copyOf(list);
  }

  private ApplicationBlock block(Element element, int i, int j) {
    var id = parseInt(element.getAttribute("id")).orElseThrow(() -> new IllegalArgumentException("Invalid block [%d][%d]".formatted(i, j)));
    var factory = requireNonNull(element.getAttribute("factory"), () -> "Block factory is null: %d".formatted(id));
    if (factory.isBlank()) {
      throw new IllegalArgumentException("Block factory is blank [%d][%d]".formatted(i, j));
    }
    if (factory.chars().allMatch(Character::isDigit) && factory.length() > 8) {
      throw new IllegalArgumentException("Invalid block factory [%d][%d]: %s".formatted(i, j, factory));
    }
    var method = requireNonNull(element.getAttribute("method"), () -> "Block method is null [%d][%d]".formatted(i, j));
    return new ApplicationBlock(id, factory, method);
  }

  private List<ApplicationLink> links(Element element) {
    var list = new LinkedList<ApplicationLink>();
    var linksNodes = element.getElementsByTagName("links");
    for (int i = 0; i < linksNodes.getLength(); i++) {
      if (linksNodes.item(i) instanceof Element links) {
        var linkNodes = links.getElementsByTagName("link");
        for (int j = 0; j < linkNodes.getLength(); j++) {
          if (linkNodes.item(j) instanceof Element link) {
            list.add(link(link, i, j));
          }
        }
      }
    }
    return List.copyOf(list);
  }

  private ApplicationLink link(Element element, int i, int j) {
    Element out = null;
    var outNodes = element.getElementsByTagName("out");
    for (int k = 0; k < outNodes.getLength(); k++) {
      if (outNodes.item(k) instanceof Element e) {
        out = e;
      }
    }
    if (out == null) {
      throw new IllegalArgumentException("Link [%d][%d] error: out is null".formatted(i, j));
    }
    Element in = null;
    var inNodes = element.getElementsByTagName("in");
    for (int k = 0; k < inNodes.getLength(); k++) {
      if (inNodes.item(k) instanceof Element e) {
        in = e;
      }
    }
    if (in == null) {
      throw new IllegalArgumentException("Link [%d][%d] error: in is null".formatted(i, j));
    }
    return new ApplicationLink(connector(out, "out", i, j), connector(in, "in", i, j));
  }

  private ApplicationConnector connector(Element element, String name, int i, int j) {
    var blockId = parseInt(element.getAttribute("block")).orElseThrow(() -> new IllegalArgumentException("Block [%d][%d].%s id = null".formatted(i, j, name)));
    var spot = requireNonNull(element.getAttribute("spot"), () -> "Block [%d][%d].%s spot is null".formatted(i, j, name));
    var index = parseInt(element.getAttribute("index"), 0).orElseThrow(() -> new IllegalArgumentException("Block [%d].%s invalid index".formatted(blockId, name)));
    return new ApplicationConnector(blockId, spot, index);
  }

  private OptionalInt parseInt(String v) {
    try {
      return OptionalInt.of(Integer.parseInt(v));
    } catch (NumberFormatException e) {
      return OptionalInt.empty();
    }
  }

  private OptionalInt parseInt(String v, int defaultValue) {
    return v.isEmpty() ? OptionalInt.of(defaultValue) : parseInt(v);
  }
}
