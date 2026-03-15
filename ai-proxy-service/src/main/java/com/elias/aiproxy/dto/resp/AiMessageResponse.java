package com.elias.aiproxy.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(name = "AiMessageResponse", description = "AI 消息响应")
public class AiMessageResponse {

    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "会话chatId")
    private String chatId;

    @Schema(description = "消息角色")
    private String role;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息类型")
    private String messageType;

    @Schema(description = "工具名称")
    private String toolName;

    @Schema(description = "工具调用ID")
    private String toolCallId;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
