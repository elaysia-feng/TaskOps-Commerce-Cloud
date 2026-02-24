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
    @Operation(summary = "创建任务", description = "需要登录态，自动绑定当前用户为 owner")
    public ApiResponse<Long> create(@Valid @RequestBody CreateTaskRequest request) {
        Long uid = UserContext.userId();
        if (uid == null) {
            throw new BizException(ErrorCode.NOT_LOGGED_IN);
        }
        return ApiResponse.ok(taskAppService.create(request, uid));
    }

    @PostMapping("/search")
    @Operation(summary = "分页搜索任务")
    public ApiResponse<IPage<InternshipTask>> search(@RequestBody TaskQueryRequest request) {
        return ApiResponse.ok(taskAppService.search(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询任务详情")
    public ApiResponse<InternshipTask> detail(
            @Parameter(description = "任务ID", required = true) @PathVariable("id") Long id) {
        return ApiResponse.ok(taskAppService.detail(id));
    }

    @GetMapping("/hot")
    @Operation(summary = "查询任务热榜", description = "返回热度 Top10")
    public ApiResponse<List<InternshipTask>> hot() {
        return ApiResponse.ok(taskAppService.hot());
    }
}
