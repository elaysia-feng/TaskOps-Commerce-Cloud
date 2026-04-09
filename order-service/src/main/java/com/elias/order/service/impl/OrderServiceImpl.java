package com.elias.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elias.common.exception.BizException;
import com.elias.common.mq.event.OrderCreatedEvent;
import com.elias.order.dto.CreateOrderRequest;
import com.elias.order.dto.CreateOrderResponse;
import com.elias.order.entity.Order;
import com.elias.order.entity.TradeOrderItem;
import com.elias.order.mapper.OrderMapper;
import com.elias.order.mapper.TradeOrderItemMapper;
import com.elias.order.mq.producer.OrderEventProducer;
import com.elias.order.service.OrderService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final String STATUS_PENDING_PAY = "PENDING_PAY";
    private static final String STATUS_PAID = "PAID";
    private static final String STATUS_DONE = "DONE";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String CURRENCY_CNY = "CNY";
    private static final String BIZ_TYPE_PRODUCT = "PRODUCT";
    private static final String BIZ_TYPE_MEMBER_PLAN = "MEMBER_PLAN";
    private static final String ITEM_TYPE_SKU = "SKU";
    private static final String ITEM_TYPE_MEMBER_PLAN = "MEMBER_PLAN";

    private static final Map<String, ProductSpec> PRODUCT_SPECS = buildProductSpecs();

    private final OrderMapper orderMapper;
    private final TradeOrderItemMapper tradeOrderItemMapper;
    private final OrderEventProducer orderEventProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateOrderResponse createOrder(CreateOrderRequest request, Long userId) {
        validateCreateOrderRequest(request);
        CreateOrderContext context = prepareCreateOrderContext(request, userId);
        executeOrderCreation(context);
        handleAfterOrderCreated(context);
        return buildCreateOrderResponse(context);
    }

    @Override
    public Order getByOrderNo(String orderNo) {
        return loadOrderByOrderNo(orderNo);
    }

    @Override
    public List<Order> listByUserId(Long userId) {
        return queryOrdersByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(String orderNo, Long userId) {
        Order order = loadOrderByOrderNo(orderNo);
        validateOrderCancellation(order, userId);
        executeOrderCancellation(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markPaid(String orderNo) {
        Order order = loadOrderByOrderNo(orderNo);
        if (order == null || isOrderAlreadyFinished(order)) {
            return;
        }

        validateOrderCanBePaid(order);
        executeOrderPaid(order);
    }

    @Override
    public Map<String, Long> summary() {
        return buildOrderSummary();
    }

    private void validateCreateOrderRequest(CreateOrderRequest request) {
        if (request == null) {
            throw new BizException(4001, "create order request can not be null");
        }
        if (!StringUtils.hasText(request.getSkuCode())) {
            throw new BizException(4002, "skuCode can not be blank");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BizException(4003, "quantity must be greater than 0");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(4004, "amount must be greater than 0");
        }

        ProductSpec spec = resolveProductSpec(request.getSkuCode());
        if (spec.isMembership()) {
            validateMembershipOrderRequest(request, spec);
        }
    }

    private CreateOrderContext prepareCreateOrderContext(CreateOrderRequest request, Long userId) {
        ProductSpec spec = resolveProductSpec(request.getSkuCode());
        CreateOrderContext context = new CreateOrderContext();
        context.setRequest(request);
        context.setSpec(spec);
        context.setOrder(buildPendingOrder(request, userId, spec));
        context.setOrderItem(buildOrderItem(request, spec));
        return context;
    }

    private Order buildPendingOrder(CreateOrderRequest request, Long userId, ProductSpec spec) {
        LocalDateTime now = LocalDateTime.now();
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setBizType(spec.isMembership() ? BIZ_TYPE_MEMBER_PLAN : BIZ_TYPE_PRODUCT);
        order.setBizNo(request.getSkuCode());
        order.setOrderTitle(spec.getTitle());
        order.setOrderDesc(spec.getDescription());
        order.setCurrencyCode(CURRENCY_CNY);
        order.setTotalAmount(request.getAmount());
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setAmount(request.getAmount());
        order.setPaidAmount(BigDecimal.ZERO);
        order.setStatus(STATUS_PENDING_PAY);
        order.setExpiredAt(now.plusMinutes(30));
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order.setSkuCode(request.getSkuCode());
        order.setQuantity(request.getQuantity());
        return order;
    }

    private TradeOrderItem buildOrderItem(CreateOrderRequest request, ProductSpec spec) {
        LocalDateTime now = LocalDateTime.now();
        TradeOrderItem item = new TradeOrderItem();
        item.setItemType(spec.isMembership() ? ITEM_TYPE_MEMBER_PLAN : ITEM_TYPE_SKU);
        item.setItemCode(request.getSkuCode());
        item.setItemName(spec.getTitle());
        item.setBizType(spec.isMembership() ? BIZ_TYPE_MEMBER_PLAN : BIZ_TYPE_PRODUCT);
        item.setBizNo(request.getSkuCode());
        item.setUnitPrice(request.getAmount().divide(BigDecimal.valueOf(request.getQuantity()), 2, RoundingMode.HALF_UP));
        item.setQuantity(request.getQuantity());
        item.setTotalAmount(request.getAmount());
        item.setItemSnapshotJson(buildItemSnapshotJson(request, spec));
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        return item;
    }

    private void executeOrderCreation(CreateOrderContext context) {
        orderMapper.insert(context.getOrder());

        TradeOrderItem item = context.getOrderItem();
        item.setOrderId(context.getOrder().getId());
        item.setOrderNo(context.getOrder().getOrderNo());
        tradeOrderItemMapper.insert(item);
    }

    private void handleAfterOrderCreated(CreateOrderContext context) {
        Order order = context.getOrder();
        orderEventProducer.sendOrderCreated(new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                order.getOrderNo(),
                order.getUserId(),
                order.getAmount()
        ));
    }

    private CreateOrderResponse buildCreateOrderResponse(CreateOrderContext context) {
        return new CreateOrderResponse(context.getOrder().getOrderNo(), context.getOrder().getStatus());
    }

    private List<Order> queryOrdersByUserId(Long userId) {
        return orderMapper.selectDetailsByUserId(userId);
    }

    private void validateOrderCancellation(Order order, Long userId) {
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException(4042, "order not found");
        }
        if (!STATUS_PENDING_PAY.equals(order.getStatus())) {
            throw new BizException(4102, "only pending order can cancel");
        }
    }

    private void executeOrderCancellation(Order order) {
        order.setStatus(STATUS_CANCELLED);
        order.setCloseReason("USER_CANCEL");
        order.setClosedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    private boolean isOrderAlreadyFinished(Order order) {
        return STATUS_PAID.equals(order.getStatus()) || STATUS_DONE.equals(order.getStatus());
    }

    private void validateOrderCanBePaid(Order order) {
        if (STATUS_CANCELLED.equals(order.getStatus())) {
            throw new BizException(4103, "cancelled order cannot be paid");
        }
    }

    private void executeOrderPaid(Order order) {
        order.setStatus(STATUS_PAID);
        order.setPaidAmount(order.getAmount());
        order.setPaidAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    private Map<String, Long> buildOrderSummary() {
        Map<String, Long> result = new HashMap<>();
        result.put("total", orderMapper.selectCount(new LambdaQueryWrapper<>()));
        result.put("pending", orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, STATUS_PENDING_PAY)));
        result.put("paid", orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, STATUS_PAID)));
        result.put("cancelled", orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, STATUS_CANCELLED)));
        return result;
    }

    private Order loadOrderByOrderNo(String orderNo) {
        return orderMapper.selectDetailByOrderNo(orderNo);
    }

    private void validateMembershipOrderRequest(CreateOrderRequest request, ProductSpec spec) {
        if (request.getQuantity() == null || request.getQuantity() != 1) {
            throw new BizException(4105, "membership order quantity must be 1");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(spec.getPrice()) != 0) {
            throw new BizException(4106, "membership order amount invalid, expected=" + spec.getPrice());
        }
    }

    private ProductSpec resolveProductSpec(String skuCode) {
        return PRODUCT_SPECS.getOrDefault(skuCode, ProductSpec.product(skuCode, skuCode, null, null));
    }

    private String buildItemSnapshotJson(CreateOrderRequest request, ProductSpec spec) {
        return "{\"skuCode\":\"" + request.getSkuCode() + "\",\"title\":\"" + spec.getTitle() + "\",\"quantity\":" + request.getQuantity() + ",\"amount\":" + request.getAmount() + "}";
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private static Map<String, ProductSpec> buildProductSpecs() {
        Map<String, ProductSpec> specs = new HashMap<>();
        specs.put("SKU_MEAL_001", ProductSpec.product("SKU_MEAL_001", "校园生活套餐", "适合跑腿、资料整理与即时服务", null));
        specs.put("SKU_MEAL_002", ProductSpec.product("SKU_MEAL_002", "创意执行套餐", "适合内容和轻技术类服务场景", null));
        specs.put("MEMBER_VIP", ProductSpec.membership("MEMBER_VIP", "VIP 会员", "兼容旧前端会员 SKU", new BigDecimal("19.90")));
        specs.put("MEMBER_SVIP", ProductSpec.membership("MEMBER_SVIP", "SVIP 会员", "兼容旧前端会员 SKU", new BigDecimal("39.90")));
        specs.put("VIP_MONTHLY", ProductSpec.membership("VIP_MONTHLY", "VIP Monthly", "TaskOps v2 member plan", new BigDecimal("19.90")));
        specs.put("SVIP_MONTHLY", ProductSpec.membership("SVIP_MONTHLY", "SVIP Monthly", "TaskOps v2 member plan", new BigDecimal("39.90")));
        specs.put("VIP_YEARLY", ProductSpec.membership("VIP_YEARLY", "VIP Yearly", "TaskOps v2 member plan", new BigDecimal("199.00")));
        specs.put("SVIP_YEARLY", ProductSpec.membership("SVIP_YEARLY", "SVIP Yearly", "TaskOps v2 member plan", new BigDecimal("399.00")));
        return specs;
    }

    @Data
    private static class CreateOrderContext {
        private CreateOrderRequest request;
        private ProductSpec spec;
        private Order order;
        private TradeOrderItem orderItem;
    }

    @Data
    private static class ProductSpec {
        private final String skuCode;
        private final String title;
        private final String description;
        private final boolean membership;
        private final BigDecimal price;

        static ProductSpec product(String skuCode, String title, String description, BigDecimal price) {
            return new ProductSpec(skuCode, title, description, false, price);
        }

        static ProductSpec membership(String skuCode, String title, String description, BigDecimal price) {
            return new ProductSpec(skuCode, title, description, true, price);
        }
    }
}