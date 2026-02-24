package com.elias.auth.controller;

import com.elias.auth.dto.LoginRequest;
import com.elias.auth.dto.LoginResponse;
import com.elias.auth.dto.RegisterRequest;
import com.elias.auth.service.AuthAppService;
import com.elias.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

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
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.ok(authAppService.login(request, httpRequest.getRemoteAddr()));
    }
}
