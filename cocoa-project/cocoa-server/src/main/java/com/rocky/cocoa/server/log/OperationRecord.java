package com.rocky.cocoa.server.log;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationRecord {
    String value() default "";
}
