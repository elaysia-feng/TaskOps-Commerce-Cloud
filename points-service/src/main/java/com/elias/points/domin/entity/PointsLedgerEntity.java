package com.elias.points.domin.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PointsLedgerEntity {
    private Long id;
    private String bizNo;
    private Long userId;
    private String changeType;
    private String sourceType;
    private Integer points;
    private Integer balanceAfter;
    private String remark;
    private LocalDateTime createdAt;
}
