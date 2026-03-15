package com.elias.aiproxy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_ai_tool_call_log")
@Schema(name = "AiToolCallLog", description = "AI 工具调用日志实体")
public class AiToolCallLog {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "所属会话chatId")
    private String chatId;

    @Schema(description = "工具调用唯一ID")
    private String toolCallId;

    @Schema(description = "工具名称")
    private String toolName;

    @Schema(description = "工具调用参数JSON")
    private String toolArgs;

    @Schema(description = "工具执行结果")
    private String toolResult;

    @Schema(description = "执行状态：1成功，0失败")
    private Integer status;

    @Schema(description = "失败原因")
    private String errorMsg;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
