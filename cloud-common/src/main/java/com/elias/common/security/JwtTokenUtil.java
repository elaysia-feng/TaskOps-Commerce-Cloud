package com.elias.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpHeaders;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
/**
 * 文件说明： JwtTokenUtil.
 * 组件职责： 项目中的通用组件。
 */

public final class JwtTokenUtil {

    private JwtTokenUtil() {
    }

    public static String resolveBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }

    public static JwtUserInfo parseUser(String token, String secret) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        String username = claims.getSubject();
        Long userId = claims.get("uid", Long.class);
        Object rolesObj = claims.get("roles");
        String roles = rolesObj == null ? "" : rolesObj.toString();
        return new JwtUserInfo(userId, username, roles);
    }

    public static String resolveBearerTokenFromHeaders(org.springframework.http.HttpHeaders headers) {
        return resolveBearerToken(headers.getFirst(HttpHeaders.AUTHORIZATION));
    }
}

