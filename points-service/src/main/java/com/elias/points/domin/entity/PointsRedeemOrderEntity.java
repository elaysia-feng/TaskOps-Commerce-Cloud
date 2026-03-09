package com.elias.points.domin.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PointsRedeemOrderEntity {
    private Long id;
    private String redeemNo;
    private Long userId;
    private String targetType;
    private Integer costPoints;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
