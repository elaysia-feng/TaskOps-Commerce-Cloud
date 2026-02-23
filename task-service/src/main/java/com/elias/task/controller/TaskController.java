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
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
/**
 * 文件说明：任务接口控制器。
 * 组件职责：提供任务创建、检索、详情、热榜等 HTTP 接口。
 */
public class TaskController {

    private final TaskAppService taskAppService;

    public TaskController(TaskAppService taskAppService) {
        this.taskAppService = taskAppService;
    }

    @PostMapping
    /**
     * 创建任务（需登录）。
     */
    public ApiResponse<Long> create(@Valid @RequestBody CreateTaskRequest request, HttpServletRequest servletRequest) {
        Long uid = UserContext.userId(servletRequest);
        if (uid == null) {
            throw new BizException(ErrorCode.NOT_LOGGED_IN);
        }
        return ApiResponse.ok(taskAppService.create(request, uid));
    }

    @PostMapping("/search")
    /**
     * 条件分页检索任务。
     */
    public ApiResponse<IPage<InternshipTask>> search(@RequestBody TaskQueryRequest request) {
        return ApiResponse.ok(taskAppService.search(request));
    }

    @GetMapping("/{id}")
    /**
     * 查询任务详情。
     */
    public ApiResponse<InternshipTask> detail(@PathVariable("id") Long id) {
        return ApiResponse.ok(taskAppService.detail(id));
    }

    @GetMapping("/hot")
    /**
     * 查询任务热榜。
     */
    public ApiResponse<List<InternshipTask>> hot() {
        return ApiResponse.ok(taskAppService.hot());
    }
}
