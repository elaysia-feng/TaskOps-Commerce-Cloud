package com.elias.auth.controller;

import com.elias.auth.dto.LoginRequest;
import com.elias.auth.dto.LoginResponse;
import com.elias.auth.dto.RegisterRequest;
import com.elias.auth.service.AuthAppService;
import com.elias.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
/**
 * 文件说明：认证外部接口控制器。
 * 组件职责：对外提供注册与登录能力。
 */
public class AuthController {

    private final AuthAppService authAppService;

    @PostMapping("/register")
    /**
     * 注册接口。
     */
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authAppService.register(request);
        return ApiResponse.ok();
    }

    @PostMapping("/login")
    /**
     * 登录接口，成功后返回 JWT。
     */
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.ok(authAppService.login(request, httpRequest.getRemoteAddr()));
    }
}
