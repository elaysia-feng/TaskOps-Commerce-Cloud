package com.elias.common.context;

import com.elias.common.security.UserHeaderNames;

import jakarta.servlet.http.HttpServletRequest;
/**
 * 文件说明： UserContext.
 * 组件职责： 项目中的通用组件。
 */

public final class UserContext {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> ROLES_HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(Long userId, String username, String roles) {
        USER_ID_HOLDER.set(userId);
        USERNAME_HOLDER.set(username);
        ROLES_HOLDER.set(roles);
    }

    public static void clear() {
        USER_ID_HOLDER.remove();
        USERNAME_HOLDER.remove();
        ROLES_HOLDER.remove();
    }

    public static Long userId() {
        return USER_ID_HOLDER.get();
    }

    public static String username() {
        String username = USERNAME_HOLDER.get();
        return username == null ? "anonymous" : username;
    }

    public static String roles() {
        String roles = ROLES_HOLDER.get();
        return roles == null ? "" : roles;
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
