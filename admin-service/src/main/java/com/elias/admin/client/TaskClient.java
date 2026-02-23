package com.elias.admin.client;

import com.elias.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@FeignClient(name = "task-service")
/**
 * 文件说明： TaskClient.
 * 组件职责： 项目中的通用组件。
 */
public interface TaskClient {
    @GetMapping("/api/tasks/hot")
    ApiResponse<List<Map<String, Object>>> hotTasks();
}
