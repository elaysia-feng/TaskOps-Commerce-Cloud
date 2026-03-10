package com.elias.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(name = "SubmitTaskRequest", description = "提交任务完成结果请求")
public class SubmitTaskRequest {

    @NotBlank
    @Schema(description = "提交说明", required = true)
    private String content;

    @Schema(description = "提交凭证URL，多个值可用 JSON 字符串或逗号分隔")
    private String proofUrls;
}