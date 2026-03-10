package com.elias.pay.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayWechatNotifyRequest {
    private String outTradeNo;
    private String transactionId;
    private String tradeState;
    private BigDecimal amount;
    private String payerId;
    private String rawContent;
}
