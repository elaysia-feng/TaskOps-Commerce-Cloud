package com.elias.task.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.elias.common.ApiResponse;
import com.elias.common.context.UserContext;
import com.elias.common.exception.BizException;
import com.elias.common.exception.ErrorCode;
import com.elias.task.dto.CancelTaskRequest;
import com.elias.task.dto.CreateTaskRequest;
import com.elias.task.dto.RejectTaskRequest;
import com.elias.task.dto.SubmitTaskRequest;
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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Task API", description = "接单平台任务创建、搜索、详情与热榜接口")
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

    @PostMapping("/create")
    @Operation(summary = "创建任务")
    public ApiResponse<Long> create(@Valid @RequestBody CreateTaskRequest request) {
        Long uid = requireLogin();

        long used = usedTaskCount(uid);
        int limit = taskQuotaService.maxTasks(uid);
        if (used >= limit) {
            throw new BizException(4104, "task publish limit reached, current limit=" + limit);
        }
        return ApiResponse.ok(taskAppService.create(request, uid));
    }

    @GetMapping("/membership/me")
    @Operation(summary = "查询我的发布配额")
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
    @Operation(summary = "切换我的会员等级（演示用）")
    public ApiResponse<Void> switchMembership(@PathVariable("level") String level) {
        Long uid = requireLogin();
        MembershipLevel membershipLevel = MembershipLevel.valueOf(level.toUpperCase());
        taskQuotaService.setLevel(uid, membershipLevel);
        return ApiResponse.ok(null);
    }

    @PostMapping("/search")
    @Operation(summary = "搜索任务")
    public ApiResponse<IPage<InternshipTask>> search(@RequestBody TaskQueryRequest request) {
        return ApiResponse.ok(taskAppService.search(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "任务详情")
    public ApiResponse<InternshipTask> detail(
            @Parameter(description = "任务ID", required = true) @PathVariable("id") Long id) {
        return ApiResponse.ok(taskAppService.detail(id));
    }

    @GetMapping("/hot")
    @Operation(summary = "热门任务 Top10")
    public ApiResponse<List<InternshipTask>> hot() {
        return ApiResponse.ok(taskAppService.hot());
    }

    @PostMapping("/{id}/accept")
    public ApiResponse<Void> acceptTask(@PathVariable("id") @NotNull Long id) {
        taskAppService.acceptTask(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<Void> submitTask(@RequestBody SubmitTaskRequest submitTaskRequest,
                                        @PathVariable("id") @NotNull Long id) {
        Long uid = requireLogin();
        taskAppService.submitTask(id, uid, submitTaskRequest);
        return ApiResponse.ok();
    }


    @PostMapping("/{id}/approve")
    public ApiResponse<Void> approveTask(@PathVariable("id") @NotNull Long id) {
        Long uid = requireLogin();
        taskAppService.approveTask(id, uid);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<Void> rejectTask(@PathVariable("id") @NotNull Long id,
                                        @RequestBody(required = false) RejectTaskRequest request) {
        Long uid = requireLogin();
        taskAppService.rejectTask(id, uid, request);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancelTask(@PathVariable Long id,
                                        @RequestBody(required = false) CancelTaskRequest request) {
        Long uid = requireLogin();
        String reason = request == null ? null : request.getReason();
        taskAppService.cancelTask(id, uid, reason);
        return ApiResponse.ok();
    }

    @GetMapping("/mine/published")
    public ApiResponse<IPage<InternshipTask>> publishedTasks(TaskQueryRequest request) {
        Long uid = requireLogin();
        return ApiResponse.ok(taskAppService.publishedTasks(uid, request));
    }

    @GetMapping("/mine/accept")
    public ApiResponse<IPage<InternshipTask>> acceptedTasks(TaskQueryRequest request) {
        Long uid = requireLogin();
        return ApiResponse.ok(taskAppService.acceptedTasks(uid, request));
    }

    @GetMapping("/mine/review")
    public ApiResponse<IPage<InternshipTask>> reviewTasks(TaskQueryRequest request) {
        Long uid = requireLogin();
        return ApiResponse.ok(taskAppService.reviewTasks(uid, request));
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
                .eq(InternshipTask::getPublisherId, uid)
                .notIn(InternshipTask::getStatus, "CANCELLED", "SETTLED"));
    }
}