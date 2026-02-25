package com.elias.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_message_consume_log")
public class MessageConsumeLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String messageId;
    private String bizType;
    private LocalDateTime createdAt;
}
