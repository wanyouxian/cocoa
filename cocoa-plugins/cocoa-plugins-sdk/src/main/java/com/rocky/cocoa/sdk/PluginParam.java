package com.rocky.cocoa.sdk;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PluginParam {

  public String name();

  public boolean isTime() default false;

  public String timeFormat() default "";

}
