package com.elias.aiproxy.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CreateSessionResponse", description = "创建 AI 会话响应")
public class CreateSessionResponse {

    @Schema(description = "会话chatId")
    private String chatId;
}
