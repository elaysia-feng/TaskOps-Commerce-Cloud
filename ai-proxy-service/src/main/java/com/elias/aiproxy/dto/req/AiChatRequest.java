package com.elias.aiproxy.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "AiChatRequest", description = "AI 对话请求")
public class AiChatRequest {

    @Schema(description = "用户输入内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String prompt;

    @Schema(description = "会话chatId，首轮对话时可为空，由后端自动创建")
    private String chatId;
}
