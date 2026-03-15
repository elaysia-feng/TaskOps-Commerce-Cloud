package com.elias.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Schema(name = "RegisterRequest", description = "注册请求")
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "seele")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需在6-32位")
    @Schema(description = "密码", example = "123456", minLength = 6, maxLength = 32)
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Schema(description = "昵称", example = "Seele")
    private String nickname;
}
