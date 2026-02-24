package com.elias.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(name = "UserInfoDTO", description = "用户信息")
public class UserInfoDTO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "状态，1=启用")
    private Integer status;

    @Schema(description = "角色列表")
    private List<String> roles;
}
