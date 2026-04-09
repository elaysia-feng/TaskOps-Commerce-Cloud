package com.elias.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_auth_identity")
public class UserAuthIdentity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String identityType;

    private String principal;

    private String credentialHash;

    private Integer credentialVersion;

    private String status;

    private LocalDateTime lastVerifiedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}