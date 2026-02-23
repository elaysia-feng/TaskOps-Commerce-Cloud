package com.elias.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
/**
 * 文件说明： LoginResponse.
 * 组件职责： 项目中的通用组件。
 */
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private List<String> roles;
}
