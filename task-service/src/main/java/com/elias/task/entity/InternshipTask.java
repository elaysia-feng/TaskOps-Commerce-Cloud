package com.elias.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("internship_task")
@Schema(name = "InternshipTask", description = "任务实体")
public class InternshipTask {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "任务归属用户ID（auth-service 用户ID）")
    private Long ownerId;

    @Schema(description = "任务标题")
    private String title;

    @Schema(description = "任务描述")
    private String description;

    @Schema(description = "技术栈")
    private String techStack;

    @Schema(description = "优先级，数值越小优先级越高")
    private Integer priority;

    @Schema(description = "状态（TODO/DOING/DONE）")
    private String status;

    @Schema(description = "进度（0-100）")
    private Integer progress;

    @Schema(description = "评论数量")
    private Integer commentCount;

    @Schema(description = "质量评分（0-100）")
    private Integer qualityScore;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
