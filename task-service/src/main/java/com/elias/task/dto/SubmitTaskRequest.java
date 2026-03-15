package com.elias.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
@Schema(name = "SubmitTaskRequest", description = "提交任务请求")
public class SubmitTaskRequest {

    @NotBlank
    @Schema(description = "提交说明", example = "任务已完成，请验收")
    private String content;

    @Schema(description = "提交凭证链接，后续可接入 MinIO 或 OSS")
    private String proofUrls;
}