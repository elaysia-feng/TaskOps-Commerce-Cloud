package com.elias.admin.client;

import com.elias.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(name = "pay-service")
public interface PayClient {
    @GetMapping("/api/pay/internal/summary")
    ApiResponse<Map<String, Long>> summary();
}
