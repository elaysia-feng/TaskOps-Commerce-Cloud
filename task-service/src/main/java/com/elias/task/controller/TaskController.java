package com.elias.task.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.elias.common.ApiResponse;
import com.elias.common.context.UserContext;
import com.elias.common.exception.BizException;
import com.elias.common.exception.ErrorCode;
import com.elias.task.dto.CreateTaskRequest;
import com.elias.task.dto.TaskQueryRequest;
import com.elias.task.entity.InternshipTask;
import com.elias.task.service.TaskAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "任务接口", description = "任务创建、检索、详情与热榜")
public class TaskController {

    private final TaskAppService taskAppService;

    public TaskController(TaskAppService taskAppService) {
        this.taskAppService = taskAppService;
    }

    @PostMapping
    @Operation(summary = "创建任务", description = "需要登录态，自动绑定当前用户为owner")
    public ApiResponse<Long> create(@Valid @RequestBody CreateTaskRequest request) {
        // 1) 读取当前登录用户ID（由网关透传后写入UserContext）
        Long uid = UserContext.userId();
        // 1.1) 用户未登录：直接中断请求
        if (uid == null) {
            throw new BizException(ErrorCode.NOT_LOGGED_IN);
        }
        // 1.2) 用户已登录：交给服务层创建任务并返回新任务ID
        return ApiResponse.ok(taskAppService.create(request, uid));
    }

    @PostMapping("/search")
    @Operation(summary = "分页搜索任务")
    public ApiResponse<IPage<InternshipTask>> search(@RequestBody TaskQueryRequest request) {
        // 2) 将分页与筛选条件透传到服务层，返回标准分页结构
        return ApiResponse.ok(taskAppService.search(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询任务详情")
    public ApiResponse<InternshipTask> detail(
            @Parameter(description = "任务ID", required = true) @PathVariable("id") Long id) {
        // 3) 查询详情（服务层会执行进度更新、热度更新、缓存失效）
        return ApiResponse.ok(taskAppService.detail(id));
    }

    @GetMapping("/hot")
    @Operation(summary = "查询任务热榜", description = "返回热度 Top10")
    public ApiResponse<List<InternshipTask>> hot() {
        // 4) 查询热榜（优先读Redis，空时触发重建）
        return ApiResponse.ok(taskAppService.hot());
    }
}
