package com.elias.aiproxy.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(name = "AiToolCallLogResponse", description = "AI 工具调用日志响应")
public class AiToolCallLogResponse {

    @Schema(description = "工具调用ID")
    private String toolCallId;

    @Schema(description = "工具名称")
    private String toolName;

    @Schema(description = "工具调用参数JSON")
    private String toolArgs;

    @Schema(description = "工具执行结果")
    private String toolResult;

    @Schema(description = "执行状态")
    private Integer status;

    @Schema(description = "失败原因")
    private String errorMsg;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
