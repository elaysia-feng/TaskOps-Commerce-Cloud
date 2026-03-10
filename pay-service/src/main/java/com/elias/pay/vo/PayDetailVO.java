package com.elias.pay.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
}
