package com.elias.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
@Data
/**
 * 文件说明： RegisterRequest.
 * 组件职责： 项目中的通用组件。
 */
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需在6-32位")
    private String password;

    @NotBlank(message = "昵称不能为空")
    private String nickname;
}
