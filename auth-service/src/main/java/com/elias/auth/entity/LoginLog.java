package com.elias.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("login_log")
/**
 * 文件说明： LoginLog.
 * 组件职责： 项目中的通用组件。
 */
public class LoginLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String ip;
    private Integer success;
    private String message;
    private LocalDateTime createdAt;
}
