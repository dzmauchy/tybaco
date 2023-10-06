package org.tybaco.runtime.logging;

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

import org.tybaco.runtime.util.Settings;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;

final class HostContext {

  final List<String> addresses;
  final List<String> hosts;
  final List<String> users;
  final long pid = ProcessHandle.current().pid();

  public HostContext() {
    var ctx = new Ctx();
    if (ctx.resolveNetworks) {
      try {
        var networks = NetworkInterface.networkInterfaces().toList();
        for (var network : networks) {
          if (network.isLoopback()) continue;
          visit(network, ctx);
        }
      } catch (IOException e) {
        e.printStackTrace(System.err);
      }
    }
    if (ctx.resolveHosts) {
      try {
        var addr = InetAddress.getLocalHost();
        ctx.addresses.add(addr.getHostAddress());
        ctx.hosts.add(addr.getHostName());
      } catch (IOException e) {
        e.printStackTrace(System.err);
      }
    }
    this.addresses = List.copyOf(ctx.addresses);
    this.hosts = List.copyOf(ctx.hosts);
    this.users = List.copyOf(ctx.users);
  }

  private void visit(NetworkInterface networkInterface, Ctx ctx) {
    var inetAddresses = networkInterface.inetAddresses().toList();
    for (var inetAddress : inetAddresses) {
      if (inetAddress.isLoopbackAddress()) continue;
      if (inetAddress.isMulticastAddress()) continue;
      if (inetAddress.isAnyLocalAddress()) continue;
      ctx.addresses.add(inetAddress.getHostAddress());
      if (ctx.resolveHosts) {
        ctx.hosts.add(inetAddress.getHostName());
      }
      if (ctx.deep) {
        var subInterfaces = networkInterface.subInterfaces().toList();
        for (var subInterface : subInterfaces) {
          visit(subInterface, ctx);
        }
      }
    }
  }

  private static final class Ctx {

    private final LinkedHashSet<String> hosts = new LinkedHashSet<>();
    private final LinkedHashSet<String> addresses = new LinkedHashSet<>();
    private final LinkedHashSet<String> users = new LinkedHashSet<>();
    private final boolean deep = Settings.booleanSetting("TY_RESOLVE_NETWORKS_DEEP").orElse(Boolean.FALSE);
    private final boolean resolveHosts = Settings.booleanSetting("TY_RESOLVE_HOSTS").orElse(Boolean.FALSE);
    private final boolean resolveNetworks = Settings.booleanSetting("TY_RESOLVE_NETWORKS").orElse(Boolean.FALSE);

    private Ctx() {
      Optional.ofNullable(System.getenv("HOSTNAME"))
        .or(() -> Optional.ofNullable(System.getenv("hostname")))
        .or(() -> Optional.ofNullable(System.getenv("HOST")))
        .or(() -> Optional.ofNullable(System.getenv("host")))
        .or(() -> Optional.ofNullable(System.getenv("COMPUTERNAME")))
        .ifPresent(hosts::add);
      users.add(System.getProperty("user.name", "anonymous"));
      ProcessHandle.current().info().user().ifPresent(users::add);
    }
  }
}
