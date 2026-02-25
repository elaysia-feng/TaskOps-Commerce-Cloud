package com.elias.pay.service;

import com.elias.pay.entity.Payment;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentService {
    void createPaymentIfAbsent(String messageId, String orderNo, BigDecimal amount);

    Payment mockPaySuccess(String orderNo);

    Map<String, Long> summary();
}
