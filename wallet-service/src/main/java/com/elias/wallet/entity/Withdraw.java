package com.elias.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_withdraw")
@Schema(name = "Withdraw", description = "提现申请实体")
public class Withdraw {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String withdrawNo;
    private Long userId;
    private BigDecimal amount;
    private String accountType;
    private String accountNo;
    private String accountName;
    private String status;
    private String remark;
    private String rejectReason;
    private Long auditBy;
    private LocalDateTime auditAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}