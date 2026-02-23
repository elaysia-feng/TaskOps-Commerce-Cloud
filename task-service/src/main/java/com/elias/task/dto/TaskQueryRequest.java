package com.elias.task.dto;

import lombok.Data;

@Data
/**
 * 文件说明： TaskQueryRequest.
 * 组件职责： 项目中的通用组件。
 */
public class TaskQueryRequest {
    private String keyword;
    private String status;
    private Integer minPriority;
    private Integer maxPriority;
    private Integer page = 1;
    private Integer size = 10;
}
