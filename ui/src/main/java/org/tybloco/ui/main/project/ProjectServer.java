package org.tybloco.ui.main.project;

/*-
 * #%L
 * ui
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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.*;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.tybloco.ui.model.Project;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.JarURLConnection;
import java.util.Optional;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

import static com.sun.net.httpserver.HttpServer.create;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.InetAddress.getLoopbackAddress;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

@Lazy(false)
public class ProjectServer {

  private static final Logger LOG = Logger.getLogger(ProjectServer.class.getName());

  private final ThreadGroup threadGroup = new ThreadGroup("http");
  private final Projects projects;
  private final HttpServer server;

  public ProjectServer(Projects projects) throws Exception {
    this.projects = projects;
    this.server = create(new InetSocketAddress(getLoopbackAddress(), 0), 10);
    this.server.createContext("/", this::on);
    this.server.setExecutor(executor());
  }

  private void on(HttpExchange exchange) throws IOException {
    var url = exchange.getRequestURI().toString();
    LOG.log(INFO, "Request {0}", url);
    if (url.startsWith("/js/") || url.startsWith("/page/")) {
      serveResource(exchange, url);
    } else if (url.startsWith("/project/")) {
      serveResource(exchange, "/project/project.html");
    }
  }

  private void serveResource(HttpExchange exchange, String res) throws IOException {
    var resource = getClass().getClassLoader().getResource("webapp" + res);
    if (resource == null) {
      LOG.log(WARNING, "Not found {0}", res);
      exchange.sendResponseHeaders(HTTP_NOT_FOUND, -1L);
      return;
    }
    var conn = resource.openConnection();
    conn.setUseCaches(true);
    conn.connect();
    var headers = exchange.getResponseHeaders();
    contentType(res).ifPresent(c -> headers.add("Content-Type", c));
    var contentSize = conn instanceof JarURLConnection c ? c.getJarEntry().getSize() : conn.getContentLength();
    LOG.log(INFO, "Preparing to write {0} bytes of {1}", new Object[] {contentSize, res});
    exchange.sendResponseHeaders(HTTP_OK, contentSize);
    try (exchange; var is = conn.getInputStream(); var os = exchange.getResponseBody()) {
      is.transferTo(os);
    }
    LOG.log(INFO, "Written {0} bytes of {1}", new Object[]{contentSize, res});
  }

  private Optional<String> contentType(String url) {
    if (url.endsWith(".js")) {
      return Optional.of("application/javascript");
    } else if (url.endsWith(".html")) {
      return Optional.of("text/html");
    } else {
      return Optional.empty();
    }
  }

  public String projectUrl(Project project) {
    return "http://127.0.0.1:" + server.getAddress().getPort() + "/project/" + project.id;
  }

  @EventListener
  private void onStart(ContextStartedEvent event) {
    server.start();
  }

  @EventListener
  private void onStop(ContextStoppedEvent event) {
    server.stop(1);
  }

  private ThreadPoolExecutor executor() {
    var threadFactory = new CustomizableThreadFactory("http");
    threadFactory.setThreadGroup(threadGroup);
    threadFactory.setDaemon(true);
    var executor = new ThreadPoolExecutor(16, 16, 1L, SECONDS, new SynchronousQueue<>(), threadFactory);
    executor.allowCoreThreadTimeOut(true);
    return executor;
  }
}
