package com.elias.admin.client;

import com.elias.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(name = "order-service")
public interface OrderClient {
    @GetMapping("/api/orders/internal/summary")
    ApiResponse<Map<String, Long>> summary();
}
