package com.elias.order.controller;

import com.elias.common.ApiResponse;
import com.elias.common.context.UserContext;
import com.elias.common.exception.BizException;
import com.elias.common.exception.ErrorCode;
import com.elias.order.dto.CreateOrderRequest;
import com.elias.order.dto.CreateOrderResponse;
import com.elias.order.entity.Order;
import com.elias.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<CreateOrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        Long uid = UserContext.userId();
        if (uid == null) {
            throw new BizException(ErrorCode.NOT_LOGGED_IN);
        }
        return ApiResponse.ok(orderService.createOrder(request, uid));
    }

    @GetMapping("/mine")
    public ApiResponse<List<Order>> mine() {
        Long uid = UserContext.userId();
        if (uid == null) {
            throw new BizException(ErrorCode.NOT_LOGGED_IN);
        }
        return ApiResponse.ok(orderService.listByUserId(uid));
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<Order> detail(@PathVariable String orderNo) {
        Order order = orderService.getByOrderNo(orderNo);
        if (order == null) {
            throw new BizException(4042, "order not found");
        }
        return ApiResponse.ok(order);
    }

    @PostMapping("/{orderNo}/cancel")
    public ApiResponse<Void> cancel(@PathVariable String orderNo) {
        Long uid = UserContext.userId();
        if (uid == null) {
            throw new BizException(ErrorCode.NOT_LOGGED_IN);
        }
        orderService.cancel(orderNo, uid);
        return ApiResponse.ok();
    }

    @GetMapping("/internal/summary")
    public ApiResponse<Map<String, Long>> summary() {
        return ApiResponse.ok(orderService.summary());
    }

    @PostMapping("/internal/{orderNo}/mark-paid")
    public ApiResponse<Void> markPaid(@PathVariable String orderNo) {
        orderService.markPaid(orderNo);
        return ApiResponse.ok();
    }
}