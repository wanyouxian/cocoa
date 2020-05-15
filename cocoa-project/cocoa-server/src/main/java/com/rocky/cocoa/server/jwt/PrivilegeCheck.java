package com.rocky.cocoa.server.jwt;

import com.rocky.cocoa.entity.system.PrivilegeType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PrivilegeCheck {
    PrivilegeType privilegeType();
}
