package com.elias.points.domin.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PointsRedeemOrderDTO {
    private String redeemNo;
    private Long userId;
    private String targetType;
    private Integer costPoints;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
