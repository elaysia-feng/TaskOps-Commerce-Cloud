package com.elias.pay.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayAlipayNotifyRequest {
    private String outTradeNo;
    private String tradeNo;
    private String tradeStatus;
    private BigDecimal totalAmount;
    private String buyerId;
    private String rawContent;
}
