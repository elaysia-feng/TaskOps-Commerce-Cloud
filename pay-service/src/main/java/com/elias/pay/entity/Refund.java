package com.elias.pay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_refund")
public class Refund {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String refundNo;
    private String payNo;
    private String orderNo;
    private BigDecimal refundAmount;
    private String status;
    private String thirdRefundNo;
    private String refundReason;
    private LocalDateTime refundTime;
    private LocalDateTime callbackTime;
    private String callbackContent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
