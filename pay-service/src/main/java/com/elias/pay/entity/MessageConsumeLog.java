package com.elias.pay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("trade_message_consume_log")
public class MessageConsumeLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String messageId;

    private String consumerName;

    private String bizType;

    private LocalDateTime createdAt;
}