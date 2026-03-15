package com.elias.aiproxy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_ai_memory")
@Schema(name = "AiMemory", description = "AI 长期记忆实体")
public class AiMemory {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "所属用户ID")
    private Long userId;

    @Schema(description = "记忆键")
    private String memoryKey;

    @Schema(description = "记忆内容")
    private String memoryContent;

    @Schema(description = "记忆摘要")
    private String memorySummary;

    @Schema(description = "记忆类型：profile/preference/fact/project")
    private String memoryType;

    @Schema(description = "来源会话chatId")
    private String sourceChatId;

    @Schema(description = "来源消息ID")
    private Long sourceMessageId;

    @Schema(description = "重要性评分")
    private Integer importanceScore;

    @Schema(description = "默认是否选中：1是，0否")
    private Integer selectedByDefault;

    @Schema(description = "状态：1有效，0失效")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
