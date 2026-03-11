package com.elias.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "TaskQueryRequest", description = "任务查询条件")
public class TaskQueryRequest {

    @Schema(description = "关键字，匹配标题/描述/标签/地点")
    private String keyword;

    @Schema(description = "任务状态", example = "OPEN")
    private String status;

    @Schema(description = "任务分类", example = "ERRAND")
    private String category;

    @Schema(description = "最小优先级")
    private Integer minPriority;

    @Schema(description = "最大优先级")
    private Integer maxPriority;

    @Schema(description = "页码，从1开始", example = "1")
    private Integer page = 1;

    @Schema(description = "每页条数", example = "10")
    private Integer size = 10;
}