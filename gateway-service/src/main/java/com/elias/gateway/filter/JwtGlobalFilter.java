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

/**
 * 文件说明：网关全局 JWT 鉴权过滤器。
 * 组件职责：
 * 1) 白名单路径直接放行。
 * 2) 非白名单路径必须携带 Bearer Token（否则返回 401）。
 * 3) Token 解析成功后，将用户信息写入请求头并透传给下游服务。
 * 4) 对 /api/admin/** 额外进行 ADMIN 角色校验（否则返回 403）。
 */
@Component
public class JwtGlobalFilter implements GlobalFilter, Ordered {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    /**
     * 核心过滤逻辑：每个请求都会先经过这里。
     */
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1) 获取当前请求路径
        String path = exchange.getRequest().getURI().getPath();
        // 2) 白名单路径直接放行
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // 3) 从请求头中提取 Bearer Token
        String token = JwtTokenUtil.resolveBearerTokenFromHeaders(exchange.getRequest().getHeaders());
        // 4) 未携带 token，返回 401
        if (token == null) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            // 5) 解析 token，得到 userId / username / roles
            JwtUserInfo userInfo = JwtTokenUtil.parseUser(token, jwtSecret);
            // 6) 管理接口做角色校验，不满足返回 403
            if (pathMatcher.match("/api/admin/**", path) && !userInfo.getRoles().contains("ADMIN")) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            // 7) 透传用户信息给下游服务，避免下游重复解析 token
            ServerHttpRequest mutated = exchange.getRequest().mutate()
                    .header(UserHeaderNames.USER_ID, String.valueOf(userInfo.getUserId()))
                    .header(UserHeaderNames.USERNAME, userInfo.getUsername())
                    .header(UserHeaderNames.ROLES, userInfo.getRoles())
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (Exception e) {
            // 8) token 非法或过期，返回 401
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    /**
     * 白名单匹配：满足任意一个 pattern 即视为公开接口。
     */
    private boolean isPublicPath(String path) {
        List<String> whiteList = List.of(
                "/api/auth/**",
                "/api/tasks/hot",
                "/api/pay/callback/mock",
                "/actuator/**"
        );
        return whiteList.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    /**
     * 过滤器执行顺序：值越小优先级越高，-100 表示尽量靠前执行。
     */
    public int getOrder() {
        return -100;
    }
}
