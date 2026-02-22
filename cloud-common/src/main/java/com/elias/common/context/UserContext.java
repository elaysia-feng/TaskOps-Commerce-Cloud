package com.elias.common.context;

import com.elias.common.security.UserHeaderNames;

import javax.servlet.http.HttpServletRequest;

public final class UserContext {

    private UserContext() {
    }

    public static Long userId(HttpServletRequest request) {
        String uid = request.getHeader(UserHeaderNames.USER_ID);
        if (uid == null || uid.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(uid);
        } catch (Exception e) {
            return null;
        }
    }

    public static String username(HttpServletRequest request) {
        String username = request.getHeader(UserHeaderNames.USERNAME);
        return username == null ? "anonymous" : username;
    }

    public static String roles(HttpServletRequest request) {
        String roles = request.getHeader(UserHeaderNames.ROLES);
        return roles == null ? "" : roles;
    }
}
