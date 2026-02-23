package com.elias.task.dto;

import lombok.Data;

import java.util.List;

@Data
/**
 * 文件说明： UserInfoDTO.
 * 组件职责： 项目中的通用组件。
 */
public class UserInfoDTO {
    private Long userId;
    private String username;
    private String nickname;
    private Integer status;
    private List<String> roles;
}
