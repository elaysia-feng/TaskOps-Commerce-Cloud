package com.elias.points.domin.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PointsAccountEntity {
    private Long id;
    private Long userId;
    private Integer balance;
    private Integer totalEarned;
    private Integer totalSpent;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
