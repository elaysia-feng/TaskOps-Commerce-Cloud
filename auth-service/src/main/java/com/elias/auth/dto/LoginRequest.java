package com.elias.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
/**
 * 文件说明： LoginRequest.
 * 组件职责： 项目中的通用组件。
 */
public class LoginRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
