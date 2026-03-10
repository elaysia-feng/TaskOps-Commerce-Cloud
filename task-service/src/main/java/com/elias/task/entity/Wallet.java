package com.elias.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_wallet")
@Schema(name = "Wallet", description = "用户钱包实体")
public class Wallet {

    @TableId(type = IdType.AUTO)
    @Schema(description = "钱包ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "可用余额")
    private BigDecimal availableAmount;

    @Schema(description = "冻结金额")
    private BigDecimal frozenAmount;

    @Schema(description = "累计收入")
    private BigDecimal totalIncome;

    @Schema(description = "累计支出")
    private BigDecimal totalExpense;

    @Schema(description = "累计提现")
    private BigDecimal totalWithdraw;

    @Schema(description = "钱包状态 ACTIVE/FROZEN/CLOSED")
    private String walletStatus;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}