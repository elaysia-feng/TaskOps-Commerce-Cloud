package com.elias.task.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.elias.common.ApiResponse;
import com.elias.common.context.UserContext;
import com.elias.common.exception.BizException;
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
public class TaskController {

    private final TaskAppService taskAppService;

    public TaskController(TaskAppService taskAppService) {
        this.taskAppService = taskAppService;
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody CreateTaskRequest request, HttpServletRequest servletRequest) {
        Long uid = UserContext.userId(servletRequest);
        if (uid == null) {
            throw new BizException(4010, "not logged in");
        }
        return ApiResponse.ok(taskAppService.create(request, uid));
    }

    @PostMapping("/search")
    public ApiResponse<IPage<InternshipTask>> search(@RequestBody TaskQueryRequest request) {
        return ApiResponse.ok(taskAppService.search(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<InternshipTask> detail(@PathVariable("id") Long id) {
        return ApiResponse.ok(taskAppService.detail(id));
    }

    @GetMapping("/hot")
    public ApiResponse<List<InternshipTask>> hot() {
        return ApiResponse.ok(taskAppService.hot());
    }
}
