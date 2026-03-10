package com.elias.task.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.elias.task.dto.CreateTaskRequest;
import com.elias.task.dto.RejectTaskRequest;
import com.elias.task.dto.SubmitTaskRequest;
import com.elias.task.dto.TaskQueryRequest;
import com.elias.task.entity.InternshipTask;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
/**
 * 文件说明： TaskAppService.
 * 组件职责： 项目中的通用组件。
 */

public interface TaskAppService {
    Long create(CreateTaskRequest request, Long ownerId);

    IPage<InternshipTask> search(TaskQueryRequest request);

    InternshipTask detail(Long id);

    List<InternshipTask> hot();

    void acceptTask(@NotBlank Long id);


    void cancelTask(Long id, Long uid, String reason);

    IPage<InternshipTask> publishedTasks(Long uid, TaskQueryRequest request);

    IPage<InternshipTask> acceptedTasks(Long uid, TaskQueryRequest request);

    IPage<InternshipTask> reviewTasks(Long uid, TaskQueryRequest request);

    void submitTask(@NotNull Long id, Long uid, SubmitTaskRequest submitTaskRequest);

    void approveTask(@NotNull Long id, Long uid);

    void rejectTask(@NotNull Long id, Long uid, RejectTaskRequest request);
}
