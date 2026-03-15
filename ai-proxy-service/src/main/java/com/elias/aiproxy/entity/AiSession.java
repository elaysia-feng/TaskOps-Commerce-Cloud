package com.elias.aiproxy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_ai_session")
@Schema(name = "AiSession", description = "AI 会话实体")
public class AiSession {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "会话唯一标识")
    private String chatId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "会话类型")
    private String sessionType;

    @Schema(description = "默认模型名称")
    private String modelName;

    @Schema(description = "是否启用记忆：1启用，0禁用")
    private Integer memoryEnabled;

    @Schema(description = "会话状态：1正常，0关闭")
    private Integer status;

    @Schema(description = "最后一条消息时间")
    private LocalDateTime lastMessageAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
