package com.elias.aiproxy.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(name = "AiSessionSummaryResponse", description = "AI 会话列表项响应")
public class AiSessionSummaryResponse {

    @Schema(description = "会话chatId")
    private String chatId;

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "会话类型")
    private String sessionType;

    @Schema(description = "默认模型名称")
    private String modelName;

    @Schema(description = "是否启用记忆")
    private Integer memoryEnabled;

    @Schema(description = "最后一条消息时间")
    private LocalDateTime lastMessageAt;
}
