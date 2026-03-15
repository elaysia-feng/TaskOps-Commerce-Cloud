package com.elias.aiproxy.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AiChatPrepareResponse", description = "聊天前会话准备响应")
public class AiChatPrepareResponse {

    @Schema(description = "会话chatId")
    private String chatId;

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "是否为本次自动创建的新会话：1是，0否")
    private Integer newlyCreated;
}
