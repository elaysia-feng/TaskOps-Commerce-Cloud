package com.elias.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_account")
@Schema(name = "User", description = "User account")
public class User {

    @TableId(type = IdType.AUTO)
    @Schema(description = "Primary key")
    private Long id;

    @Schema(description = "User number")
    private String userNo;

    @Schema(description = "Username")
    private String username;

    @Schema(description = "Nickname")
    private String nickname;

    @Schema(description = "Account status")
    private String status;

    @Schema(description = "Register source")
    private String registerSource;

    @Schema(description = "Password set flag")
    private Integer passwordSet;

    @Schema(description = "Last login time")
    private LocalDateTime lastLoginAt;

    @Schema(description = "Last login IP")
    private String lastLoginIp;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;

    @Schema(description = "Updated at")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    @Schema(description = "Transient password for compatibility")
    private String password;
}