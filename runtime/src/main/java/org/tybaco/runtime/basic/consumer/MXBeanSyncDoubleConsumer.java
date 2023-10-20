package org.tybaco.runtime.basic.consumer;

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

import org.tybaco.runtime.basic.mx.DoubleConsumerBean;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

public final class MXBeanSyncDoubleConsumer implements DoubleConsumer, AutoCloseable {

  private final SynchronousQueue<Double> queue = new SynchronousQueue<>(true);
  private final DoubleConsumerBeanImpl bean = new DoubleConsumerBeanImpl();
  private final ObjectInstance instance;
  private final Consumer<? super Throwable> onError;

  public MXBeanSyncDoubleConsumer(String domain, String key, String value, Consumer<? super Throwable> onError) throws JMException {
    var objectName = new ObjectName(domain, key, value);
    var server = ManagementFactory.getPlatformMBeanServer();
    this.instance = server.registerMBean(bean, objectName);
    this.onError = onError;
  }

  @Override
  public void accept(double value) {
    while (bean.running) {
      try {
        if (queue.offer(value, 1L, TimeUnit.SECONDS)) break;
      } catch (Throwable e) {
        onError.accept(e);
      }
    }
  }

  @Override
  public void close() throws Exception {
    bean.running = false;
    var server = ManagementFactory.getPlatformMBeanServer();
    server.unregisterMBean(instance.getObjectName());
  }

  public final class DoubleConsumerBeanImpl implements DoubleConsumerBean {

    private volatile boolean running = true;

    @Override
    public double getValue() throws InterruptedException {
      while (running) {
        var e = queue.poll(1L, TimeUnit.SECONDS);
        if (e != null) return e;
      }
      throw new CancellationException();
    }
  }
}
