package com.elias.pay.service;

import com.elias.pay.entity.Payment;
import com.elias.pay.vo.PayCreateVO;
import com.elias.pay.vo.PayDetailVO;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.Map;

public interface PaymentService {
    void createPaymentIfAbsent(String messageId, String orderNo, BigDecimal amount);

    Payment mockPaySuccess(String orderNo);

    Map<String, Long> summary();

    PayCreateVO createPayOrder(String orderNo);

    PayDetailVO getPaymentDetail(@NotBlank String orderNo);

    void closePayOrder(@NotBlank String orderNo);
}
