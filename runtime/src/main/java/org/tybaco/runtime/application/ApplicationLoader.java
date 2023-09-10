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
      requireNonNull(root.getAttribute("name"), "name attribute is null"),
      blocks(root),
      links(root)
    );
    Application.CURRENT_APPLICATION.set(application);
  }

  private List<Block> blocks(Element element) {
    var list = new LinkedList<Block>();
    var blocksNodes = element.getElementsByTagName("blocks");
    for (int i = 0; i < blocksNodes.getLength(); i++) {
      if (blocksNodes.item(i) instanceof Element blocks) {
        var blockNodes = blocks.getElementsByTagName("block");
        for (int j = 0; j < blockNodes.getLength(); j++) {
          if (blockNodes.item(j) instanceof Element block) {
            list.add(block(block, i, j));
          }
        }
      }
    }
    return List.copyOf(list);
  }

  private Block block(Element element, int i, int j) {
    final int id;
    try {
      id = Integer.parseInt(element.getAttribute("id"));
    } catch (RuntimeException e) {
      throw new IllegalArgumentException("Invalid block [%d][%d]".formatted(i, j));
    }
    var name = requireNonNull(
      element.getAttribute("name"),
      () -> "Block name is null [%d][%d]".formatted(i, j)
    );
    var factory = requireNonNull(
      element.getAttribute("factory"),
      () -> "Block factory is null [%d][%d]".formatted(i, j)
    );
    if (factory.isBlank()) {
      throw new IllegalArgumentException("Block factory is blank [%d][%d]".formatted(i, j));
    }
    if (Character.isDigit(factory.charAt(0))) {
      try {
        Integer.parseInt(factory);
      } catch (RuntimeException e) {
        throw new IllegalArgumentException("Invalid block factory [%d][%d]".formatted(i, j), e);
      }
    }
    var value = requireNonNull(
      element.getAttribute("value"),
      () -> "Block value is null [%d][%d]".formatted(i, j)
    );
    return new Block(id, name, factory, value);
  }

  private List<Link> links(Element element) {
    var list = new LinkedList<Link>();
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

  private Link link(Element element, int i, int j) {
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
    return new Link(
      connector(out, "out", i, j),
      connector(in, "in", i, j)
    );
  }

  private Connector connector(Element element, String name, int i, int j) {
    final int blockId;
    try {
      blockId = Integer.parseInt(element.getAttribute("block"));
    } catch (RuntimeException e) {
      throw new IllegalArgumentException("Block [%d][%d].%s id = null".formatted(i, j, name));
    }
    var spot = requireNonNull(
      element.getAttribute("spot"),
      () -> "Block [%d][%d].%s spot is null".formatted(i, j, name)
    );
    return new Connector(blockId, spot);
  }
}
