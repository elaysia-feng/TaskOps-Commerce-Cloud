package com.elias.wallet.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WalletFlowVO {
    private String flowNo;
    private String flowType;
    private String bizType;
    private String bizId;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String direction;
    private String remark;
    private LocalDateTime createdAt;
}