package com.elias.auth.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserInfoDTO {
    private Long userId;
    private String username;
    private String nickname;
    private Integer status;
    private List<String> roles;
}
