package com.elias.admin.controller;

import com.elias.admin.client.AuthClient;
import com.elias.admin.client.TaskClient;
import com.elias.common.ApiResponse;
import com.elias.common.context.UserContext;
import com.elias.common.exception.BizException;
import com.elias.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理接口", description = "管理端聚合查询接口")
public class AdminController {

    private final AuthClient authClient;
    private final TaskClient taskClient;

    public AdminController(AuthClient authClient, TaskClient taskClient) {
        this.authClient = authClient;
        this.taskClient = taskClient;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "管理看板", description = "需要 ADMIN 角色，聚合登录日志与热榜任务")
    public ApiResponse<Map<String, Object>> dashboard(
            @Parameter(description = "登录日志条数，默认30") @RequestParam(defaultValue = "30") int loginLogLimit) {
        String roles = UserContext.roles();
        if (!roles.contains("ADMIN")) {
            throw new BizException(ErrorCode.FORBIDDEN_ADMIN_ROLE_REQUIRED);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("loginLogs", authClient.loginLogs(loginLogLimit).getData());
        result.put("hotTasks", taskClient.hotTasks().getData());
        return ApiResponse.ok(result);
    }
}
