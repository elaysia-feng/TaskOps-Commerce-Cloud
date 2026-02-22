package com.elias.auth.controller;

import com.elias.auth.dto.UserInfoDTO;
import com.elias.auth.entity.LoginLog;
import com.elias.auth.service.AuthAppService;
import com.elias.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/auth")
public class AuthInternalController {

    private final AuthAppService authAppService;

    public AuthInternalController(AuthAppService authAppService) {
        this.authAppService = authAppService;
    }

    @GetMapping("/users/{id}")
    public ApiResponse<UserInfoDTO> userInfo(@PathVariable("id") Long userId) {
        return ApiResponse.ok(authAppService.getUserInfo(userId));
    }

    @GetMapping("/login-logs")
    public ApiResponse<List<LoginLog>> loginLogs(@RequestParam(defaultValue = "30") int limit) {
        return ApiResponse.ok(authAppService.latestLoginLogs(limit));
    }
}
