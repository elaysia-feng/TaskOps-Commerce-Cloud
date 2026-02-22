package com.elias.admin.client;

import com.elias.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "auth-service")
public interface AuthClient {
    @GetMapping("/internal/auth/login-logs")
    ApiResponse<List<Map<String, Object>>> loginLogs(@RequestParam("limit") int limit);
}
