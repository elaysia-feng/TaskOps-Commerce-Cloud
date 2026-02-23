package com.elias.task.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.elias.task.dto.CreateTaskRequest;
import com.elias.task.dto.TaskQueryRequest;
import com.elias.task.entity.InternshipTask;

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
}
