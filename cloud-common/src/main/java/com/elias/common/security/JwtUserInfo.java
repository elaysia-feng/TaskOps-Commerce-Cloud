package com.elias.common.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
/**
 * 文件说明： JwtUserInfo.
 * 组件职责： 项目中的通用组件。
 */
public class JwtUserInfo {
    private Long userId;
    private String username;
    private String roles;
}

