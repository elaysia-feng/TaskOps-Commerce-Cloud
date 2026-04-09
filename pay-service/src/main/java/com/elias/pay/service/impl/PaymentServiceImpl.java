package com.elias.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elias.common.ApiResponse;
import com.elias.common.exception.BizException;
import com.elias.common.mq.event.PaySuccessEvent;
import com.elias.pay.client.OrderClient;
import com.elias.pay.dto.OrderInfoDTO;
import com.elias.pay.entity.MessageConsumeLog;
import com.elias.pay.entity.Payment;
import com.elias.pay.mapper.MessageConsumeLogMapper;
import com.elias.pay.mapper.PaymentMapper;
import com.elias.pay.mq.producer.PayEventProducer;
import com.elias.pay.service.PaymentService;
import com.elias.pay.vo.PayCreateVO;
import com.elias.pay.vo.PayDetailVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String BIZ_TYPE_ORDER_CREATED = "ORDER_CREATED";
    private static final String CONSUMER_NAME_ORDER_CREATED = "pay-service:order-created";
    private static final String STATUS_INIT = "INIT";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_CLOSED = "CLOSED";
    private static final String CHANNEL_MOCK = "MOCK";
    private static final String PAY_SCENE_WEB = "WEB";

    private final PaymentMapper paymentMapper;
    private final MessageConsumeLogMapper consumeLogMapper;
    private final OrderClient orderClient;
    private final PayEventProducer payEventProducer;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPaymentIfAbsent(String messageId, String orderNo, BigDecimal amount) {
        CreatePaymentContext context = prepareCreatePaymentContext(messageId, orderNo, amount);
        if (context.isAlreadyConsumed()) {
            return;
        }

        executePaymentCreationIfNeeded(context);
        recordOrderCreatedConsumption(context);
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public Payment mockPaySuccess(String orderNo) {
        PaySuccessContext context = preparePaySuccessContext(orderNo);
        executePaymentSuccess(context);
        syncOrderPayState(context);
        handleAfterPaySuccess(context);
        return context.getPayment();
    }

    @Override
    public Map<String, Long> summary() {
        return buildPaymentSummary();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayCreateVO createPayOrder(String orderNo) {
        CreatePayOrderContext context = prepareCreatePayOrderContext(orderNo);
        validatePayOrderCreation(context);
        executePayOrderInitialization(context);
        return buildPayCreateVO(context.getPayment());
    }

    @Override
    public PayDetailVO getPaymentDetail(String orderNo) {
        Payment payment = loadRequiredPayment(orderNo);
        return buildPayDetailVO(payment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closePayOrder(String orderNo) {
        Payment payment = loadRequiredPayment(orderNo);
        validatePayOrderCanBeClosed(payment);
        executePayOrderClose(payment);
    }

    private CreatePaymentContext prepareCreatePaymentContext(String messageId, String orderNo, BigDecimal amount) {
        CreatePaymentContext context = new CreatePaymentContext();
        context.setMessageId(messageId);
        context.setOrderNo(orderNo);
        context.setAmount(amount);
        context.setAlreadyConsumed(hasConsumedOrderCreatedMessage(messageId));
        context.setPayment(loadPaymentByOrderNo(orderNo));
        if (context.getPayment() == null) {
            context.setOrder(loadOrderDetail(orderNo));
        }
        return context;
    }

    private boolean hasConsumedOrderCreatedMessage(String messageId) {
        return consumeLogMapper.selectOne(new LambdaQueryWrapper<MessageConsumeLog>()
                .eq(MessageConsumeLog::getMessageId, messageId)
                .eq(MessageConsumeLog::getConsumerName, CONSUMER_NAME_ORDER_CREATED)
                .eq(MessageConsumeLog::getBizType, BIZ_TYPE_ORDER_CREATED)) != null;
    }

    private void executePaymentCreationIfNeeded(CreatePaymentContext context) {
        if (context.getPayment() != null) {
            return;
        }

        Payment payment = buildInitPayment(context.getOrder());
        paymentMapper.insert(payment);
        context.setPayment(payment);
    }

    private void recordOrderCreatedConsumption(CreatePaymentContext context) {
        MessageConsumeLog row = new MessageConsumeLog();
        row.setMessageId(context.getMessageId());
        row.setConsumerName(CONSUMER_NAME_ORDER_CREATED);
        row.setBizType(BIZ_TYPE_ORDER_CREATED);
        row.setCreatedAt(LocalDateTime.now());
        consumeLogMapper.insert(row);
    }

    private PaySuccessContext preparePaySuccessContext(String orderNo) {
        PaySuccessContext context = new PaySuccessContext();
        context.setPayment(loadRequiredPayment(orderNo));
        return context;
    }

    private void executePaymentSuccess(PaySuccessContext context) {
        Payment payment = context.getPayment();
        if (STATUS_SUCCESS.equals(payment.getStatus())) {
            return;
        }

        payment.setStatus(STATUS_SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        paymentMapper.updateById(payment);
    }

    private void syncOrderPayState(PaySuccessContext context) {
        Payment payment = context.getPayment();
        ApiResponse<Void> markPaidResp = orderClient.markPaid(payment.getOrderNo());
        if (!isOk(markPaidResp)) {
            throw new BizException(5003, "order mark paid failed after pay success");
        }

        ApiResponse<OrderInfoDTO> detailResp = orderClient.detail(payment.getOrderNo());
        if (!isOk(detailResp) || detailResp.getData() == null) {
            throw new BizException(5002, "order detail not found after pay success");
        }

        context.setOrder(detailResp.getData());
    }

    private void handleAfterPaySuccess(PaySuccessContext context) {
        Payment payment = context.getPayment();
        OrderInfoDTO order = context.getOrder();

        payEventProducer.sendPaySuccess(new PaySuccessEvent(
                UUID.randomUUID().toString(),
                payment.getOrderNo(),
                payment.getPayNo(),
                order.getUserId(),
                order.getSkuCode(),
                payment.getAmount()
        ));
    }

    private Map<String, Long> buildPaymentSummary() {
        Map<String, Long> result = new HashMap<>();
        result.put("total", paymentMapper.selectCount(new LambdaQueryWrapper<>()));
        result.put("pending", paymentMapper.selectCount(
                new LambdaQueryWrapper<Payment>().in(Payment::getStatus, STATUS_INIT, STATUS_PENDING)
        ));
        result.put("success", paymentMapper.selectCount(
                new LambdaQueryWrapper<Payment>().eq(Payment::getStatus, STATUS_SUCCESS)
        ));
        return result;
    }

    private CreatePayOrderContext prepareCreatePayOrderContext(String orderNo) {
        CreatePayOrderContext context = new CreatePayOrderContext();
        context.setOrderNo(orderNo);
        context.setPayment(loadPaymentByOrderNo(orderNo));
        if (context.getPayment() == null) {
            context.setOrder(loadOrderDetail(orderNo));
        }
        return context;
    }

    private void validatePayOrderCreation(CreatePayOrderContext context) {
        Payment payment = context.getPayment();
        if (payment != null) {
            if (STATUS_SUCCESS.equals(payment.getStatus())) {
                throw new BizException(4108, "order already paid");
            }
            if (STATUS_CLOSED.equals(payment.getStatus())) {
                throw new BizException(4109, "payment already closed");
            }
            return;
        }

        if (!"PENDING_PAY".equals(context.getOrder().getStatus())) {
            throw new BizException(4107, "order status not support pay");
        }
    }

    private void executePayOrderInitialization(CreatePayOrderContext context) {
        if (context.getPayment() != null) {
            return;
        }

        Payment payment = buildInitPayment(context.getOrder());
        paymentMapper.insert(payment);
        context.setPayment(payment);
    }

    private Payment buildInitPayment(OrderInfoDTO order) {
        LocalDateTime now = LocalDateTime.now();
        Payment payment = new Payment();
        payment.setPayNo(generatePayNo());
        payment.setOrderNo(order.getOrderNo());
        payment.setBuyerUserId(order.getUserId());
        payment.setChannel(CHANNEL_MOCK);
        payment.setPayScene(PAY_SCENE_WEB);
        payment.setSubject(resolveSubject(order.getSkuCode()));
        payment.setAmount(order.getAmount());
        payment.setStatus(STATUS_INIT);
        payment.setPayUrl("mock://pay/" + order.getOrderNo());
        payment.setPayParamsJson(buildPayParamsJson(order, payment));
        payment.setExpiredAt(now.plusMinutes(30));
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);
        return payment;
    }

    private PayDetailVO buildPayDetailVO(Payment payment) {
        PayDetailVO payDetailVO = new PayDetailVO();
        payDetailVO.setPayNo(payment.getPayNo());
        payDetailVO.setOrderNo(payment.getOrderNo());
        payDetailVO.setChannel(payment.getChannel());
        payDetailVO.setStatus(payment.getStatus());
        payDetailVO.setAmount(payment.getAmount());
        payDetailVO.setSubject(payment.getSubject());
        payDetailVO.setThirdTradeNo(payment.getThirdTradeNo());
        payDetailVO.setBuyerId(resolveBuyerId(payment));
        payDetailVO.setPayTime(payment.getPaidAt());
        payDetailVO.setPayParams(parsePayParams(payment.getPayParamsJson()));
        return payDetailVO;
    }

    private void validatePayOrderCanBeClosed(Payment payment) {
        if (STATUS_SUCCESS.equals(payment.getStatus())) {
            throw new BizException(4110, "paid payment can not be closed");
        }
    }

    private void executePayOrderClose(Payment payment) {
        if (STATUS_CLOSED.equals(payment.getStatus())) {
            return;
        }

        payment.setStatus(STATUS_CLOSED);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentMapper.updateById(payment);
    }

    private Payment loadRequiredPayment(String orderNo) {
        Payment payment = loadPaymentByOrderNo(orderNo);
        if (payment == null) {
            throw new BizException(4043, "payment not found");
        }
        return payment;
    }

    private Payment loadPaymentByOrderNo(String orderNo) {
        return paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>().eq(Payment::getOrderNo, orderNo)
        );
    }

    private OrderInfoDTO loadOrderDetail(String orderNo) {
        ApiResponse<OrderInfoDTO> orderDetail = orderClient.detail(orderNo);
        if (!isOk(orderDetail) || orderDetail.getData() == null) {
            throw new BizException(4042, "order not found");
        }
        return orderDetail.getData();
    }

    private PayCreateVO buildPayCreateVO(Payment payment) {
        PayCreateVO vo = new PayCreateVO();
        vo.setPayNo(payment.getPayNo());
        vo.setOrderNo(payment.getOrderNo());
        vo.setChannel(payment.getChannel());
        vo.setStatus(payment.getStatus());
        vo.setAmount(payment.getAmount());
        vo.setSubject(payment.getSubject());
        vo.setPayParams(parsePayParams(payment.getPayParamsJson()));
        return vo;
    }

    private String generatePayNo() {
        return "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String resolveSubject(String skuCode) {
        if ("MEMBER_VIP".equals(skuCode) || "VIP_MONTHLY".equals(skuCode) || "VIP_YEARLY".equals(skuCode)) {
            return "VIP membership";
        }
        if ("MEMBER_SVIP".equals(skuCode) || "SVIP_MONTHLY".equals(skuCode) || "SVIP_YEARLY".equals(skuCode)) {
            return "SVIP membership";
        }
        return "TaskOps order payment";
    }

    private String buildPayParamsJson(OrderInfoDTO order, Payment payment) {
        Map<String, Object> params = new HashMap<>();
        params.put("mode", CHANNEL_MOCK);
        params.put("payNo", payment.getPayNo());
        params.put("orderNo", order.getOrderNo());
        params.put("amount", order.getAmount());
        params.put("subject", payment.getSubject());
        params.put("payUrl", payment.getPayUrl());
        try {
            return objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException ex) {
            throw new BizException(5004, "build pay params failed");
        }
    }

    private Map<String, Object> parsePayParams(String payParamsJson) {
        if (payParamsJson == null || payParamsJson.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(payParamsJson, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException ex) {
            throw new BizException(5005, "parse pay params failed");
        }
    }

    private String resolveBuyerId(Payment payment) {
        if (payment.getBuyerAccount() != null && !payment.getBuyerAccount().isBlank()) {
            return payment.getBuyerAccount();
        }
        return payment.getBuyerUserId() == null ? null : String.valueOf(payment.getBuyerUserId());
    }

    private boolean isOk(ApiResponse<?> response) {
        return response != null && response.getCode() == 0;
    }

    @Data
    private static class CreatePaymentContext {
        private String messageId;
        private String orderNo;
        private BigDecimal amount;
        private boolean alreadyConsumed;
        private Payment payment;
        private OrderInfoDTO order;
    }

    @Data
    private static class PaySuccessContext {
        private Payment payment;
        private OrderInfoDTO order;
    }

    @Data
    private static class CreatePayOrderContext {
        private String orderNo;
        private Payment payment;
        private OrderInfoDTO order;
    }
}