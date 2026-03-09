package com.elias.points.domin.dto;

import lombok.Data;

@Data
public class PointsRuleDTO {
    private String ruleCode;
    private String sourceType;
    private String skuCode;
    private Integer points;
    private Integer enabled;
    private String remark;
}
