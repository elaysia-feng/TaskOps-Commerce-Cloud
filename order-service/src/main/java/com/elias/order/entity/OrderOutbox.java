package com.elias.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_order_outbox")
public class OrderOutbox {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String messageId;
    private String routingKey;
    private String payload;
    private Integer status;
    private Integer retryCount;
    private LocalDateTime nextRetryTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
