package com.rocky.cocoa.server.log;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationObj {
    String value() default "";
}
