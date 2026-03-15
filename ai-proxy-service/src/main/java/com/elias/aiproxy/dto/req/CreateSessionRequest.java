package com.elias.aiproxy.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "CreateSessionRequest", description = "创建 AI 会话请求")
public class CreateSessionRequest {

    @Schema(description = "会话类型", example = "chat")
    private String sessionType = "chat";

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "默认模型名称")
    private String modelName;

    @Schema(description = "是否启用记忆：1启用，0禁用")
    private Integer memoryEnabled = 1;
}
