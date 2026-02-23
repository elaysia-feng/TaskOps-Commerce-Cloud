package com.elias.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("internship_task")
/**
 * 文件说明： InternshipTask.
 * 组件职责： 项目中的通用组件。
 */
public class InternshipTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ownerId;
    private String title;
    private String description;
    private String techStack;
    private Integer priority;
    private String status;
    private Integer progress;
    private Integer commentCount;
    private Integer qualityScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
