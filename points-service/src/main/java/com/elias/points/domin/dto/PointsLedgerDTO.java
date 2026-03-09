package com.elias.points.domin.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PointsLedgerDTO {
    private String bizNo;
    private Long userId;
    private String changeType;
    private String sourceType;
    private Integer points;
    private Integer balanceAfter;
    private String remark;
    private LocalDateTime createdAt;
}
