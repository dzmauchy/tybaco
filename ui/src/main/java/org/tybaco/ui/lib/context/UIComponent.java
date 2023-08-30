package org.tybaco.ui.lib.context;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Component
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface UIComponent {
  @AliasFor(annotation = Component.class, attribute = "value")
  String value() default "";
}
