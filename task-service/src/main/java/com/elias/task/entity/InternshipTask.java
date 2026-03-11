package com.elias.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("internship_task")
@Schema(name = "InternshipTask", description = "接单平台任务实体")
public class InternshipTask {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "发布方用户ID")
    private Long publisherId;

    @Schema(description = "接单方用户ID")
    private Long acceptorId;

    @Schema(description = "任务标题")
    private String title;

    @Schema(description = "任务描述")
    private String description;

    @Schema(description = "任务分类")
    private String category;

    @Schema(description = "任务标签")
    private String tags;

    @Schema(description = "任务地点")
    private String location;

    @Schema(description = "联系方式")
    private String contactInfo;

    @Schema(description = "任务赏金")
    private BigDecimal rewardAmount;

    @Schema(description = "平台服务费")
    private BigDecimal serviceFee;

    @Schema(description = "实际结算金额")
    private BigDecimal settleAmount;

    @Schema(description = "关联交易单号")
    private String tradeOrderNo;

    @Schema(description = "优先级，数值越小优先级越高")
    private Integer priority;

    @Schema(description = "状态 OPEN/TAKEN/SUBMITTED/SETTLEMENT_PENDING/SETTLED/CANCELLED")
    private String status;

    @Schema(description = "截止时间")
    private LocalDateTime deadline;

    @Schema(description = "是否要求提交凭证")
    private Boolean proofRequired;

    @Schema(description = "浏览次数")
    private Integer viewCount;

    @Schema(description = "评论数量")
    private Integer commentCount;

    @Schema(description = "质量评分")
    private Integer qualityScore;

    @Schema(description = "接单时间")
    private LocalDateTime acceptedAt;

    @Schema(description = "提交成果时间")
    private LocalDateTime submittedAt;

    @Schema(description = "验收通过时间")
    private LocalDateTime approvedAt;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

    @Schema(description = "取消时间")
    private LocalDateTime cancelledAt;

    @Schema(description = "驳回原因")
    private String rejectReason;

    @Schema(description = "取消原因")
    private String cancelReason;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}