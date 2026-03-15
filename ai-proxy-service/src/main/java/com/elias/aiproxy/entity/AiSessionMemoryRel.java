package com.elias.aiproxy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_ai_session_memory_rel")
@Schema(name = "AiSessionMemoryRel", description = "AI 会话与记忆关联实体")
public class AiSessionMemoryRel {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "会话chatId")
    private String chatId;

    @Schema(description = "记忆ID")
    private Long memoryId;

    @Schema(description = "是否选中：1选中，0未选中")
    private Integer selected;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
