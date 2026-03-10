package com.elias.pay.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class PayCreateVO {
    private String payNo;
    private String orderNo;
    private String channel;
    private String status;
    private BigDecimal amount;
    private String subject;
    private Map<String, Object> payParams;
}
