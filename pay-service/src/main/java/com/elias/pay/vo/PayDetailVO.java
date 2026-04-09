package com.elias.pay.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class PayDetailVO {
    private String payNo;
    private String orderNo;
    private String channel;
    private String status;
    private BigDecimal amount;
    private String subject;
    private String thirdTradeNo;
    private String buyerId;
    private LocalDateTime payTime;
    private Map<String, Object> payParams;
}