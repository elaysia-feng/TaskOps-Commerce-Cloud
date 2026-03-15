package com.elias.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_wallet_flow")
@Schema(name = "WalletFlow", description = "钱包流水实体")
public class WalletFlow {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
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
    private LocalDateTime updatedAt;
}