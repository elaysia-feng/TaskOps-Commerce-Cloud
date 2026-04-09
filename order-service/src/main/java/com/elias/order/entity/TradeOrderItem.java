package com.elias.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("trade_order_item")
public class TradeOrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String orderNo;

    private String itemType;

    private String itemCode;

    private String itemName;

    private String bizType;

    private String bizNo;

    private BigDecimal unitPrice;

    private Integer quantity;

    private BigDecimal totalAmount;

    private String itemSnapshotJson;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}