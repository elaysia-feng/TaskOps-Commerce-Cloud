package com.elias.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@Schema(name = "CreateTaskRequest", description = "创建任务请求")
public class CreateTaskRequest {

    @NotBlank
    @Schema(description = "任务标题", example = "完成任务模块联调")
    private String title;

    @NotBlank
    @Schema(description = "任务描述", example = "联调 task-service 与 auth-service")
    private String description;

    @Schema(description = "技术栈", example = "Spring Boot, OpenFeign, Redis, MySQL")
    private String techStack;

    @Min(1)
    @Max(5)
    @Schema(description = "优先级，1-5，数值越小优先级越高", example = "3")
    private Integer priority;
}
