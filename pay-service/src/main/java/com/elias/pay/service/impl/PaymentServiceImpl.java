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
import io.seata.spring.annotation.GlobalTransactional;
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

    private final PaymentMapper paymentMapper;
    private final MessageConsumeLogMapper consumeLogMapper;
    private final OrderClient orderClient;
    private final PayEventProducer payEventProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPaymentIfAbsent(String messageId, String orderNo, BigDecimal amount) {
        MessageConsumeLog existedLog = consumeLogMapper.selectOne(new LambdaQueryWrapper<MessageConsumeLog>()
                .eq(MessageConsumeLog::getMessageId, messageId)
                .eq(MessageConsumeLog::getBizType, "ORDER_CREATED"));
        if (existedLog != null) {
            return;
        }

        Payment existedPay = paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>().eq(Payment::getOrderNo, orderNo)
        );
        if (existedPay == null) {
            Payment payment = new Payment();
            payment.setPayNo("PAY" + System.currentTimeMillis()
                    + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
            payment.setOrderNo(orderNo);
            payment.setAmount(amount);
            payment.setStatus("PENDING");
            payment.setCreatedAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());
            paymentMapper.insert(payment);
        }

        MessageConsumeLog row = new MessageConsumeLog();
        row.setMessageId(messageId);
        row.setBizType("ORDER_CREATED");
        row.setCreatedAt(LocalDateTime.now());
        consumeLogMapper.insert(row);
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public Payment mockPaySuccess(String orderNo) {
        Payment payment = paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>().eq(Payment::getOrderNo, orderNo)
        );
        if (payment == null) {
            throw new BizException(4043, "payment not found");
        }

        if (!"SUCCESS".equals(payment.getStatus())) {
            payment.setStatus("SUCCESS");
            payment.setUpdatedAt(LocalDateTime.now());
            paymentMapper.updateById(payment);
        }

        orderClient.markPaid(payment.getOrderNo());

        ApiResponse<OrderInfoDTO> detailResp = orderClient.detail(payment.getOrderNo());
        if (detailResp == null || detailResp.getData() == null) {
            throw new BizException(5002, "order detail not found after pay success");
        }
        OrderInfoDTO order = detailResp.getData();

        payEventProducer.sendPaySuccess(new PaySuccessEvent(
                UUID.randomUUID().toString(),
                payment.getOrderNo(),
                payment.getPayNo(),
                order.getUserId(),
                order.getSkuCode(),
                payment.getAmount()
        ));
        return payment;
    }

    @Override
    public Map<String, Long> summary() {
        Map<String, Long> result = new HashMap<>();
        result.put("total", paymentMapper.selectCount(new LambdaQueryWrapper<>()));
        result.put("pending", paymentMapper.selectCount(
                new LambdaQueryWrapper<Payment>().eq(Payment::getStatus, "PENDING")
        ));
        result.put("success", paymentMapper.selectCount(
                new LambdaQueryWrapper<Payment>().eq(Payment::getStatus, "SUCCESS")
        ));
        return result;
    }
}
