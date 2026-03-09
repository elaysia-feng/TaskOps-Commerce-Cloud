package com.elias.pay.client.fallback;

import com.elias.common.ApiResponse;
import com.elias.pay.client.OrderClient;
import com.elias.pay.dto.OrderInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderClientFallback implements FallbackFactory<OrderClient> {
    @Override
    public OrderClient create(Throwable cause) {

        return new OrderClient() {
            @Override
            public ApiResponse<OrderInfoDTO> detail(String orderNo) {
                log.error("OrderClient.detail fallback, orderNo={}", orderNo, cause);
                return new ApiResponse<>(5001, "order-service unavailable, detail fallback", null);
            }

            @Override
            public ApiResponse<Void> markPaid(String orderNo) {
                log.error("OrderClient.markPaid fallback, orderNo={}", orderNo, cause);
                return ApiResponse.fail(5001, "order-service unavailable, markPaid fallback");
            }
        };
    }
}
