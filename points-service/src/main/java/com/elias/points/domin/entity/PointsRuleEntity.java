package com.elias.points.domin.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PointsRuleEntity {
    private Long id;
    private String ruleCode;
    private String sourceType;
    private String skuCode;
    private Integer points;
    private Integer enabled;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
