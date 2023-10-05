package org.tybaco.runtime.logging;

import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoggingServiceProviderTest {

  private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
  private final LoggingServiceProvider serviceProvider = new LoggingServiceProvider(outputStream);

  @Test
  void logSimple() throws Exception {
    var loggerFactory = serviceProvider.getLoggerFactory();
    var logger = loggerFactory.getLogger("abc");
    logger.info("Hello");
    outputStream.writeTo(System.out);
  }

  @AfterAll
  void afterAll() {
    serviceProvider.close();
  }
}
