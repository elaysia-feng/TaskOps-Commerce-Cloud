package com.elias.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Schema(name = "LoginResponse", description = "登录响应")
public class LoginResponse {

    @Schema(description = "JWT 访问令牌")
    private String token;

    @Schema(description = "用户ID", example = "4")
    private Long userId;

    @Schema(description = "用户名", example = "seele")
    private String username;

    @Schema(description = "角色列表", example = "[\"USER\"]")
    private List<String> roles;
}
