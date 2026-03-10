package com.elias.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_task_submission")
@Schema(name = "TaskSubmission", description = "任务提交记录实体")
public class TaskSubmission {

    @TableId(type = IdType.AUTO)
    @Schema(description = "提交记录ID")
    private Long id;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "提交人ID")
    private Long submitUserId;

    @Schema(description = "第几次提交")
    private Integer roundNo;

    @Schema(description = "提交说明")
    private String content;

    @Schema(description = "提交凭证URL")
    private String proofUrls;

    @Schema(description = "提交状态 SUBMITTED/APPROVED/REJECTED")
    private String status;

    @Schema(description = "本次提交驳回原因")
    private String rejectReason;

    @Schema(description = "提交时间")
    private LocalDateTime submittedAt;

    @Schema(description = "审核时间")
    private LocalDateTime reviewedAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}