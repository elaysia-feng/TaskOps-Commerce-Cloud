package com.elias.task.dto;

import lombok.Data;

@Data
public class TaskQueryRequest {
    private String keyword;
    private String status;
    private Integer minPriority;
    private Integer maxPriority;
    private Integer page = 1;
    private Integer size = 10;
}
