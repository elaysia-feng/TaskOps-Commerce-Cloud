package com.elias.pay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment_order")
public class Payment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String payNo;

    private String orderNo;

    private Long buyerUserId;

    private String channel;

    private String payScene;

    private String subject;

    private BigDecimal amount;

    private String status;

    private String thirdTradeNo;

    private String buyerAccount;

    private String payUrl;

    @TableField("pay_params_json")
    private String payParamsJson;

    private String failReason;

    private String callbackContent;

    private LocalDateTime expiredAt;

    @TableField("paid_at")
    private LocalDateTime paidAt;

    @TableField("callback_at")
    private LocalDateTime callbackAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}