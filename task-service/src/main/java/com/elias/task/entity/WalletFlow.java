package com.elias.task.entity;

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
    @Schema(description = "流水ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "流水号")
    private String flowNo;

    @Schema(description = "流水类型 TASK_INCOME/WITHDRAW/REFUND/CONSUME/FREEZE/UNFREEZE")
    private String flowType;

    @Schema(description = "业务类型 TASK/ORDER/WITHDRAW")
    private String bizType;

    @Schema(description = "业务ID")
    private String bizId;

    @Schema(description = "变动金额")
    private BigDecimal amount;

    @Schema(description = "变动前余额")
    private BigDecimal balanceBefore;

    @Schema(description = "变动后余额")
    private BigDecimal balanceAfter;

    @Schema(description = "变动方向 IN/OUT")
    private String direction;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}