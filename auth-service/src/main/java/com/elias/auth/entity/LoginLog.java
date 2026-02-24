package com.elias.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("login_log")
@Schema(name = "LoginLog", description = "登录日志")
public class LoginLog {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "IP地址")
    private String ip;

    @Schema(description = "是否成功，1=成功，0=失败")
    private Integer success;

    @Schema(description = "结果描述")
    private String message;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
