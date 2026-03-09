package com.elias.task.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.elias.common.ApiResponse;
import com.elias.common.context.UserContext;
import com.elias.common.exception.BizException;
import com.elias.common.exception.ErrorCode;
import com.elias.task.dto.CreateTaskRequest;
import com.elias.task.dto.TaskQueryRequest;
import com.elias.task.entity.InternshipTask;
import com.elias.task.mapper.InternshipTaskMapper;
import com.elias.task.membership.MembershipLevel;
import com.elias.task.membership.TaskQuotaService;
import com.elias.task.service.TaskAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Task API", description = "Task create/search/detail/hot and 简单会员配额演示")
public class TaskController {

    private final TaskAppService taskAppService;
    private final InternshipTaskMapper taskMapper;
    private final TaskQuotaService taskQuotaService;

    public TaskController(TaskAppService taskAppService,
                          InternshipTaskMapper taskMapper,
                          TaskQuotaService taskQuotaService) {
        this.taskAppService = taskAppService;
        this.taskMapper = taskMapper;
        this.taskQuotaService = taskQuotaService;
    }

    @PostMapping
    @Operation(summary = "Create task")
    public ApiResponse<Long> create(@Valid @RequestBody CreateTaskRequest request) {
        Long uid = requireLogin();

        long used = usedTaskCount(uid);
        int limit = taskQuotaService.maxTasks(uid);
        if (used >= limit) {
            throw new BizException(4104, "task limit reached, current limit=" + limit);
        }
        return ApiResponse.ok(taskAppService.create(request, uid));
    }

    @GetMapping("/membership/me")
    @Operation(summary = "Get my membership quota")
    public ApiResponse<Map<String, Object>> membership() {
        Long uid = requireLogin();
        MembershipLevel level = taskQuotaService.getLevel(uid);
        long used = usedTaskCount(uid);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", uid);
        result.put("level", level.name());
        result.put("maxTasks", level.maxTasks());
        result.put("usedTasks", used);
        result.put("remaining", Math.max(0, level.maxTasks() - used));
        return ApiResponse.ok(result);
    }

    @PutMapping("/membership/me/{level}")
    @Operation(summary = "Switch my membership level (demo only)")
    public ApiResponse<Void> switchMembership(@PathVariable("level") String level) {
        Long uid = requireLogin();
        MembershipLevel membershipLevel = MembershipLevel.valueOf(level.toUpperCase());
        taskQuotaService.setLevel(uid, membershipLevel);
        return ApiResponse.ok(null);
    }

    @PostMapping("/search")
    @Operation(summary = "Search task page")
    public ApiResponse<IPage<InternshipTask>> search(@RequestBody TaskQueryRequest request) {
        return ApiResponse.ok(taskAppService.search(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Task detail")
    public ApiResponse<InternshipTask> detail(
            @Parameter(description = "task id", required = true) @PathVariable("id") Long id) {
        return ApiResponse.ok(taskAppService.detail(id));
    }

    @GetMapping("/hot")
    @Operation(summary = "Task hot top10")
    public ApiResponse<List<InternshipTask>> hot() {
        return ApiResponse.ok(taskAppService.hot());
    }

    private Long requireLogin() {
        Long uid = UserContext.userId();
        if (uid == null) {
            throw new BizException(ErrorCode.NOT_LOGGED_IN);
        }
        return uid;
    }

    private long usedTaskCount(Long uid) {
        return taskMapper.selectCount(new LambdaQueryWrapper<InternshipTask>()
                .eq(InternshipTask::getOwnerId, uid));
    }
}
