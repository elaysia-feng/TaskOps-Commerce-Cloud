package com.elias.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("trade_order")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    @TableField("buyer_user_id")
    private Long userId;

    private String bizType;

    private String bizNo;

    private String orderTitle;

    private String orderDesc;

    private String currencyCode;

    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    @TableField("payable_amount")
    private BigDecimal amount;

    private BigDecimal paidAmount;

    private String status;

    private String closeReason;

    private LocalDateTime expiredAt;

    private LocalDateTime paidAt;

    private LocalDateTime closedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer version;

    @TableField(exist = false)
    private String skuCode;

    @TableField(exist = false)
    private Integer quantity;
}