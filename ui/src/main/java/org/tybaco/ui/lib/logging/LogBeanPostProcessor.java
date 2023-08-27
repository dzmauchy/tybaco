package org.tybaco.ui.lib.logging;

import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.context.support.GenericApplicationContext;

import java.util.logging.LogManager;

public final class LogBeanPostProcessor implements DestructionAwareBeanPostProcessor {

  private final String id;

  public LogBeanPostProcessor(GenericApplicationContext context) {
    id = context.getId();
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) {
    info("Initializing {0}", beanName);
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) {
    info("Initialized {0}", beanName);
    return bean;
  }

  @Override
  public void postProcessBeforeDestruction(Object bean, String beanName) {
    info("Destructing {0}", beanName);
  }

  private void info(String message, Object... args) {
    var info = Logging.info(message, args);
    info.setSourceClassName(null);
    info.setLoggerName(id);
    LogManager.getLogManager().getLogger("").log(info);
  }
}
