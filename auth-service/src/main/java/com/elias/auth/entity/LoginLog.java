package com.elias.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("auth_login_log")
@Schema(name = "LoginLog", description = "Login log")
public class LoginLog {

    @TableId(type = IdType.AUTO)
    @Schema(description = "Primary key")
    private Long id;

    @Schema(description = "User ID")
    private Long userId;

    @Schema(description = "Username")
    private String username;

    @Schema(description = "Login type")
    private String loginType;

    @Schema(description = "Success flag")
    private Integer success;

    @Schema(description = "Login IP")
    private String ip;

    @TableField("fail_reason")
    @Schema(description = "Result message")
    private String message;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;
}