package com.elias.pay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("refund_order")
public class Refund {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String refundNo;

    private String payNo;

    private String orderNo;

    private Long buyerUserId;

    private BigDecimal refundAmount;

    private String status;

    private String thirdRefundNo;

    private String refundReason;

    @TableField("refunded_at")
    private LocalDateTime refundedAt;

    @TableField("callback_at")
    private LocalDateTime callbackAt;

    private String callbackContent;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}