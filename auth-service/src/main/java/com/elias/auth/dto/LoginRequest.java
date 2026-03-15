package com.elias.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
@Schema(name = "LoginRequest", description = "登录请求")
public class LoginRequest {

    @NotBlank
    @Schema(description = "用户名", example = "seele")
    private String username;

    @NotBlank
    @Schema(description = "密码", example = "123456")
    private String password;
}
