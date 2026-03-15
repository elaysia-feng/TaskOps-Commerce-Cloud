package com.elias.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_wallet")
@Schema(name = "Wallet", description = "钱包账户实体")
public class Wallet {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private BigDecimal availableAmount;
    private BigDecimal frozenAmount;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal totalWithdraw;
    private String walletStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}