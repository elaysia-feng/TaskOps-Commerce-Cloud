package com.elias.aiproxy.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(name = "AiMemoryResponse", description = "AI 记忆响应")
public class AiMemoryResponse {

    @Schema(description = "记忆ID")
    private Long id;

    @Schema(description = "记忆键")
    private String memoryKey;

    @Schema(description = "记忆内容")
    private String memoryContent;

    @Schema(description = "记忆摘要")
    private String memorySummary;

    @Schema(description = "记忆类型")
    private String memoryType;

    @Schema(description = "重要性评分")
    private Integer importanceScore;

    @Schema(description = "默认是否选中")
    private Integer selectedByDefault;

    @Schema(description = "当前会话是否选中")
    private Integer selected;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
