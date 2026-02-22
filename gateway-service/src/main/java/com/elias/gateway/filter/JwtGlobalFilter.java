package com.elias.gateway.filter;

import com.elias.common.security.JwtTokenUtil;
import com.elias.common.security.JwtUserInfo;
import com.elias.common.security.UserHeaderNames;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtGlobalFilter implements GlobalFilter, Ordered {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }
        String token = JwtTokenUtil.resolveBearerTokenFromHeaders(exchange.getRequest().getHeaders());
        if (token == null) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        try {
            JwtUserInfo userInfo = JwtTokenUtil.parseUser(token, jwtSecret);
            if (pathMatcher.match("/api/admin/**", path) && !userInfo.getRoles().contains("ADMIN")) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
            ServerHttpRequest mutated = exchange.getRequest().mutate()
                    .header(UserHeaderNames.USER_ID, String.valueOf(userInfo.getUserId()))
                    .header(UserHeaderNames.USERNAME, userInfo.getUsername())
                    .header(UserHeaderNames.ROLES, userInfo.getRoles())
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublicPath(String path) {
        List<String> whiteList = List.of(
                "/api/auth/**",
                "/api/tasks/hot",
                "/actuator/**"
        );
        return whiteList.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
