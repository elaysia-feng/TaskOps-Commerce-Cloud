package com.elias.task.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.elias.task.dto.CreateTaskRequest;
import com.elias.task.dto.TaskQueryRequest;
import com.elias.task.entity.InternshipTask;

import java.util.List;

public interface TaskAppService {
    Long create(CreateTaskRequest request, Long ownerId);

    IPage<InternshipTask> search(TaskQueryRequest request);

    InternshipTask detail(Long id);

    List<InternshipTask> hot();
}
