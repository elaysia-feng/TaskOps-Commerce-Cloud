package com.elias.aiproxy.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "SessionMemorySelectRequest", description = "会话记忆选中状态变更请求")
public class SessionMemorySelectRequest {

    @NotBlank
    @Schema(description = "会话chatId", requiredMode = Schema.RequiredMode.REQUIRED)
    private String chatId;

    @NotNull
    @Schema(description = "记忆ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long memoryId;

    @NotNull
    @Schema(description = "是否选中：1选中，0不选中", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer selected;
}
