package com.elias.aiproxy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_ai_message")
@Schema(name = "AiMessage", description = "AI 对话消息实体")
public class AiMessage {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "所属会话chatId")
    private String chatId;

    @Schema(description = "消息角色：user/assistant/system/tool")
    private String role;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "会话内顺序号")
    private Integer seqNo;

    @Schema(description = "消息类型：text/function_call/function_result")
    private String messageType;

    @Schema(description = "工具名称")
    private String toolName;

    @Schema(description = "工具调用ID")
    private String toolCallId;

    @Schema(description = "使用的模型名称")
    private String modelName;

    @Schema(description = "输入token数")
    private Integer promptTokens;

    @Schema(description = "输出token数")
    private Integer completionTokens;

    @Schema(description = "总token数")
    private Integer totalTokens;

    @Schema(description = "消息状态：1成功，0失败")
    private Integer status;

    @Schema(description = "失败原因")
    private String errorMsg;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
