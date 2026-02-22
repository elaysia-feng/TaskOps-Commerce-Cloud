package com.elias.admin.controller;

import com.elias.admin.client.AuthClient;
import com.elias.admin.client.TaskClient;
import com.elias.common.ApiResponse;
import com.elias.common.context.UserContext;
import com.elias.common.exception.BizException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AuthClient authClient;
    private final TaskClient taskClient;

    public AdminController(AuthClient authClient, TaskClient taskClient) {
        this.authClient = authClient;
        this.taskClient = taskClient;
    }

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> dashboard(@RequestParam(defaultValue = "30") int loginLogLimit,
                                                      HttpServletRequest request) {
        String roles = UserContext.roles(request);
        if (!roles.contains("ADMIN")) {
            throw new BizException(4030, "admin role required");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("loginLogs", authClient.loginLogs(loginLogLimit).getData());
        result.put("hotTasks", taskClient.hotTasks().getData());
        return ApiResponse.ok(result);
    }
}
