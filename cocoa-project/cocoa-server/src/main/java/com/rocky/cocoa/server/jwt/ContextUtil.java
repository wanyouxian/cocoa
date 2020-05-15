package com.rocky.cocoa.server.jwt;

import com.rocky.cocoa.entity.system.User;

public class ContextUtil {
    private static ThreadLocal<User> local = new ThreadLocal<>();

    public static void setCurrentUser(User user) {
        local.set(user);
    }

    public static User getCurrentUser() {
        return local.get();
    }
}
