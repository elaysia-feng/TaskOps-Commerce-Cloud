package com.elias.wallet.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletOverviewVO {
    private Long userId;
    private BigDecimal availableAmount;
    private BigDecimal frozenAmount;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal totalWithdraw;
    private String walletStatus;
}