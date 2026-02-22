package com.elias.common.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtUserInfo {
    private Long userId;
    private String username;
    private String roles;
}

