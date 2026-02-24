package com.elias.auth.controller;

import com.elias.auth.dto.UserInfoDTO;
import com.elias.auth.entity.LoginLog;
import com.elias.auth.service.AuthAppService;
import com.elias.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/auth")
@Tag(name = "认证内部接口", description = "供其他微服务调用的用户与登录日志接口")
public class AuthInternalController {

    private final AuthAppService authAppService;

    public AuthInternalController(AuthAppService authAppService) {
        this.authAppService = authAppService;
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "按用户ID查询用户信息")
    public ApiResponse<UserInfoDTO> userInfo(
            @Parameter(description = "用户ID", required = true) @PathVariable("id") Long userId) {
        return ApiResponse.ok(authAppService.getUserInfo(userId));
    }

    @GetMapping("/login-logs")
    @Operation(summary = "查询最近登录日志")
    public ApiResponse<List<LoginLog>> loginLogs(
            @Parameter(description = "返回条数，默认30，最大200") @RequestParam(defaultValue = "30") int limit) {
        return ApiResponse.ok(authAppService.latestLoginLogs(limit));
    }
}
