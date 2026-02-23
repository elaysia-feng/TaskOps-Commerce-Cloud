package com.elias.task.client;

import com.elias.common.ApiResponse;
import com.elias.task.dto.UserInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service")
/**
 * 文件说明： AuthClient.
 * 组件职责： 项目中的通用组件。
 */
public interface AuthClient {
    @GetMapping("/internal/auth/users/{id}")
    ApiResponse<UserInfoDTO> userInfo(@PathVariable("id") Long userId);
}
