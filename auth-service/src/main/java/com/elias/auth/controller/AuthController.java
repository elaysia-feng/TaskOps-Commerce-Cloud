package com.elias.auth.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.elias.auth.dto.LoginRequest;
import com.elias.auth.dto.LoginResponse;
import com.elias.auth.dto.RegisterRequest;
import com.elias.auth.service.AuthAppService;
import com.elias.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证接口", description = "对外提供注册与登录能力")
public class AuthController {

    private final AuthAppService authAppService;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建用户并绑定默认 USER 角色")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authAppService.register(request);
        return ApiResponse.ok();
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "校验用户名密码并返回 JWT")
    @SentinelResource(value = "auth_login", blockHandler = "handleLoginBlocked")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        LoginResponse response = authAppService.login(request, httpRequest.getRemoteAddr());

        // JWT 之外，额外保存一份服务端会话态（可用于风控、后台会话管理）
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute("LOGIN_UID", response.getUserId());
        session.setAttribute("LOGIN_USERNAME", response.getUsername());
        session.setAttribute("LOGIN_TIME", System.currentTimeMillis());

        return ApiResponse.ok(response);
    }

    @GetMapping("/session/me")
    @Operation(summary = "会话查询", description = "返回当前 Session 登录信息")
    public ApiResponse<Map<String, Object>> sessionMe(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ApiResponse.ok(new HashMap<>());
        }
        Map<String, Object> data = new HashMap<>();
        data.put("sessionId", session.getId());
        data.put("userId", session.getAttribute("LOGIN_UID"));
        data.put("username", session.getAttribute("LOGIN_USERNAME"));
        data.put("loginTime", session.getAttribute("LOGIN_TIME"));
        return ApiResponse.ok(data);
    }

    public ApiResponse<LoginResponse> handleLoginBlocked(
            LoginRequest request, HttpServletRequest httpRequest, BlockException ex) {
        return new ApiResponse<>(429, "too many requests, login blocked by sentinel", null);
    }
}
