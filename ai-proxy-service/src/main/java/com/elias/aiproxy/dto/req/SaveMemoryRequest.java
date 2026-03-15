package com.elias.aiproxy.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "SaveMemoryRequest", description = "新增或编辑记忆请求")
public class SaveMemoryRequest {

    @Schema(description = "记忆ID，新增时为空")
    private Long id;

    @Schema(description = "记忆键")
    private String memoryKey;

    @NotBlank
    @Schema(description = "记忆内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String memoryContent;

    @Schema(description = "记忆摘要")
    private String memorySummary;

    @Schema(description = "记忆类型")
    private String memoryType = "profile";

    @Schema(description = "重要性评分")
    private Integer importanceScore = 50;

    @Schema(description = "默认是否选中：1是，0否")
    private Integer selectedByDefault = 0;
}
