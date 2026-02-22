package com.elias.task.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
public class CreateTaskRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    private String techStack;
    @Min(1)
    @Max(5)
    private Integer priority;
}
