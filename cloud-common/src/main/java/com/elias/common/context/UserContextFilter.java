package com.elias.common.context;

import com.elias.common.security.UserHeaderNames;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Bind user headers from gateway into ThreadLocal for current request.
 */
public class UserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Long uid = parseLong(request.getHeader(UserHeaderNames.USER_ID));
            String username = request.getHeader(UserHeaderNames.USERNAME);
            String roles = request.getHeader(UserHeaderNames.ROLES);
            UserContext.set(uid, username, roles);
            filterChain.doFilter(request, response);
        } finally {
            // Always clear ThreadLocal to avoid data leak across reused threads.
            UserContext.clear();
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
