package com.elias.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elias.common.exception.BizException;
import com.elias.order.dto.CreateOrderRequest;
import com.elias.order.dto.CreateOrderResponse;
import com.elias.order.entity.Order;
import com.elias.order.mapper.OrderMapper;
import com.elias.order.mapper.StockMapper;
import com.elias.order.service.OrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final StockMapper stockMapper;

    @Override
    @GlobalTransactional
    public CreateOrderResponse createOrder(CreateOrderRequest request, Long userId) {
        int updated = stockMapper.deductStock(request.getSkuCode(), request.getQuantity());
        if (updated == 0) {
            throw new BizException(4101, "stock not enough");
        }

        String orderNo = "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setSkuCode(request.getSkuCode());
        order.setQuantity(request.getQuantity());
        order.setAmount(request.getAmount());
        order.setStatus("PENDING_PAY");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.insert(order);

        return new CreateOrderResponse(orderNo, order.getStatus());
    }

    @Override
    public Order getByOrderNo(String orderNo) {
        return orderMapper.selectOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
    }

    @Override
    @GlobalTransactional
    public void cancel(String orderNo, Long userId) {
        Order order = getByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException(4042, "order not found");
        }
        if (!"PENDING_PAY".equals(order.getStatus())) {
            throw new BizException(4102, "only pending order can cancel");
        }
        order.setStatus("CANCELLED");
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        stockMapper.restoreStock(order.getSkuCode(), order.getQuantity());
    }

    @Override
    @GlobalTransactional
    public void markPaid(String orderNo) {
        Order order = getByOrderNo(orderNo);
        if (order == null) {
            return;
        }
        if ("PAID".equals(order.getStatus()) || "DONE".equals(order.getStatus())) {
            return;
        }
        order.setStatus("PAID");
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    public Map<String, Long> summary() {
        Map<String, Long> result = new HashMap<>();
        result.put("total", orderMapper.selectCount(new LambdaQueryWrapper<>()));
        result.put("pending", orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, "PENDING_PAY")));
        result.put("paid", orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, "PAID")));
        result.put("cancelled", orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, "CANCELLED")));
        return result;
    }
}
