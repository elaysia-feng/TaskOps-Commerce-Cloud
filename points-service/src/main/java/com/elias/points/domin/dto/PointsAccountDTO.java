package com.elias.points.domin.dto;

import lombok.Data;

@Data
public class PointsAccountDTO {
    private Long userId;
    private Integer balance;
    private Integer totalEarned;
    private Integer totalSpent;
}
