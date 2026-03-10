package com.elias.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_task_settlement")
@Schema(name = "TaskSettlement", description = "任务结算记录实体")
public class TaskSettlement {

    @TableId(type = IdType.AUTO)
    @Schema(description = "结算记录ID")
    private Long id;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "提交记录ID")
    private Long submissionId;

    @Schema(description = "发布方ID")
    private Long publisherId;

    @Schema(description = "接单方ID")
    private Long acceptorId;

    @Schema(description = "结算金额")
    private BigDecimal settleAmount;

    @Schema(description = "结算状态 PENDING/SUCCESS/FAILED")
    private String status;

    @Schema(description = "失败原因")
    private String failReason;

    @Schema(description = "消息ID")
    private String messageId;

    @Schema(description = "结算完成时间")
    private LocalDateTime settledAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}