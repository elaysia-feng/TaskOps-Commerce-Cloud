package com.elias.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elias.common.exception.BizException;
import com.elias.common.mq.MqConstants;
import com.elias.common.mq.event.PaySuccessEvent;
import com.elias.pay.entity.MessageConsumeLog;
import com.elias.pay.entity.Payment;
import com.elias.pay.mapper.MessageConsumeLogMapper;
import com.elias.pay.mapper.PaymentMapper;
import com.elias.pay.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public void createPaymentIfAbsent(String messageId, String orderNo, BigDecimal amount) {
        MessageConsumeLog existedLog = consumeLogMapper.selectOne(new LambdaQueryWrapper<MessageConsumeLog>()
                .eq(MessageConsumeLog::getMessageId, messageId)
                .eq(MessageConsumeLog::getBizType, "ORDER_CREATED"));
        if (existedLog != null) {
            return;
        }

        Payment existedPay = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>().eq(Payment::getOrderNo, orderNo));
        if (existedPay == null) {
            Payment payment = new Payment();
            payment.setPayNo("PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
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
    @Transactional
    public Payment mockPaySuccess(String orderNo) {
        Payment payment = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>().eq(Payment::getOrderNo, orderNo));
        if (payment == null) {
            throw new BizException(4043, "payment not found");
        }
        if (!"SUCCESS".equals(payment.getStatus())) {
            payment.setStatus("SUCCESS");
            payment.setUpdatedAt(LocalDateTime.now());
            paymentMapper.updateById(payment);
        }

        String msgId = UUID.randomUUID().toString();
        PaySuccessEvent event = new PaySuccessEvent(msgId, payment.getOrderNo(), payment.getPayNo(), payment.getAmount());
        rabbitTemplate.convertAndSend(MqConstants.EXCHANGE_BUSINESS, MqConstants.RK_PAY_SUCCESS, event, message -> {
            message.getMessageProperties().setMessageId(msgId);
            return message;
        });
        return payment;
    }

    @Override
    public Map<String, Long> summary() {
        Map<String, Long> result = new HashMap<>();
        result.put("total", paymentMapper.selectCount(new LambdaQueryWrapper<>()));
        result.put("pending", paymentMapper.selectCount(new LambdaQueryWrapper<Payment>().eq(Payment::getStatus, "PENDING")));
        result.put("success", paymentMapper.selectCount(new LambdaQueryWrapper<Payment>().eq(Payment::getStatus, "SUCCESS")));
        return result;
    }
}
