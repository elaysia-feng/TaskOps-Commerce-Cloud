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
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
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
            payment.setPayTime(LocalDateTime.now());
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayCreateVO createPayOrder(String orderNo) {
        Payment payment = paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>().eq(Payment::getOrderNo, orderNo)
        );
        if (payment != null) {
            if ("SUCCESS".equals(payment.getStatus())) {
                throw new BizException(4108, "order already paid");
            }
            if ("CLOSED".equals(payment.getStatus())) {
                throw new BizException(4109, "payment already closed");
            }
            return buildPayCreateVO(payment);
        }

        ApiResponse<OrderInfoDTO> orderDetail = orderClient.detail(orderNo);
        if (orderDetail == null || orderDetail.getData() == null) {
            throw new BizException(4042, "order not found");
        }
        OrderInfoDTO order = orderDetail.getData();
        if (!"PENDING_PAY".equals(order.getStatus())) {
            throw new BizException(4107, "order status not support pay");
        }

        Payment newPayment = new Payment();
        newPayment.setPayNo("PAY" + System.currentTimeMillis()
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        newPayment.setOrderNo(order.getOrderNo());
        newPayment.setChannel("MOCK");
        newPayment.setSubject(resolveSubject(order.getSkuCode()));
        newPayment.setAmount(order.getAmount());
        newPayment.setStatus("INIT");
        newPayment.setCreatedAt(LocalDateTime.now());
        newPayment.setUpdatedAt(LocalDateTime.now());
        paymentMapper.insert(newPayment);

        return buildPayCreateVO(newPayment);
    }

    @Override
    public PayDetailVO getPaymentDetail(String orderNo) {
        Payment payment = paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>().eq(Payment::getOrderNo, orderNo)
        );
        if (payment == null) {
            throw new BizException(4043, "payment not found");
        }

        PayDetailVO payDetailVO = new PayDetailVO();
        BeanUtils.copyProperties(payment, payDetailVO);
        return payDetailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closePayOrder(String orderNo) {
        Payment payment = paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>().eq(Payment::getOrderNo, orderNo)
        );
        if (payment == null) {
            throw new BizException(4043, "payment not found");
        }
        if ("SUCCESS".equals(payment.getStatus())) {
            throw new BizException(4110, "paid payment can not be closed");
        }
        if ("CLOSED".equals(payment.getStatus())) {
            return;
        }

        payment.setStatus("CLOSED");
        payment.setUpdatedAt(LocalDateTime.now());
        paymentMapper.updateById(payment);
    }

    private PayCreateVO buildPayCreateVO(Payment payment) {
        PayCreateVO vo = new PayCreateVO();
        vo.setPayNo(payment.getPayNo());
        vo.setOrderNo(payment.getOrderNo());
        vo.setChannel(payment.getChannel());
        vo.setStatus(payment.getStatus());
        vo.setAmount(payment.getAmount());
        vo.setSubject(payment.getSubject());
        return vo;
    }

    private String resolveSubject(String skuCode) {
        if ("MEMBER_VIP".equals(skuCode)) {
            return "VIP membership";
        }
        if ("MEMBER_SVIP".equals(skuCode)) {
            return "SVIP membership";
        }
        return "TaskOps order payment";
    }
}
