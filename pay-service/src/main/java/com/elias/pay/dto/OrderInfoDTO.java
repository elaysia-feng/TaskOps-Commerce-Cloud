package com.elias.pay.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderInfoDTO {
    private String orderNo;
    private Long userId;
    private String skuCode;
    private Integer quantity;
    private BigDecimal amount;
    private String status;
}
