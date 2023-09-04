package org.tybaco.ui.main.services;

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
import org.springframework.context.event.*;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.tybaco.ui.lib.context.EagerComponent;
import org.tybaco.ui.model.Project;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static com.sun.net.httpserver.HttpServer.create;
import static java.net.InetAddress.getLoopbackAddress;
import static java.util.concurrent.TimeUnit.SECONDS;

@EagerComponent
public class ProjectServer {

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
    // language=html
    var response = """
      <html>
        <body bgcolor='black'>
        </body>
      </html>
      """;
    var raw = response.getBytes(StandardCharsets.UTF_8);
    exchange.sendResponseHeaders(200, raw.length);
    try (exchange; var os = exchange.getResponseBody()) {
      os.write(raw);
    }
  }

  public String projectUrl(Project project) {
    return "http://127.0.0.1:" + server.getAddress().getPort() + "/page/" + project.id;
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
