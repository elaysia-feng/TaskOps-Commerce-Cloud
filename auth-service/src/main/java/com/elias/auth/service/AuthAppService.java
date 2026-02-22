package com.elias.auth.service;

import com.elias.auth.dto.LoginRequest;
import com.elias.auth.dto.LoginResponse;
import com.elias.auth.dto.RegisterRequest;
import com.elias.auth.dto.UserInfoDTO;
import com.elias.auth.entity.LoginLog;

import java.util.List;

public interface AuthAppService {
    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request, String ip);

    UserInfoDTO getUserInfo(Long userId);

    List<LoginLog> latestLoginLogs(int limit);
}
