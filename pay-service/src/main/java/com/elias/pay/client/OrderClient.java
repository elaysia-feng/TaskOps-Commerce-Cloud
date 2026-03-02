package com.elias.pay.client;

import com.elias.common.ApiResponse;
import com.elias.pay.client.fallback.OrderClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service", fallbackFactory = OrderClientFallback.class)
public interface OrderClient {

    @PostMapping("/api/orders/internal/{orderNo}/mark-paid")
    ApiResponse<Void> markPaid(@PathVariable("orderNo") String orderNo);
}
