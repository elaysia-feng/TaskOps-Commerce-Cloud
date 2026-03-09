package com.elias.order.service;

import com.elias.order.dto.CreateOrderRequest;
import com.elias.order.dto.CreateOrderResponse;
import com.elias.order.entity.Order;

import java.util.List;
import java.util.Map;

public interface OrderService {
    CreateOrderResponse createOrder(CreateOrderRequest request, Long userId);

    Order getByOrderNo(String orderNo);

    List<Order> listByUserId(Long userId);

    void cancel(String orderNo, Long userId);

    void markPaid(String orderNo);

    Map<String, Long> summary();
}