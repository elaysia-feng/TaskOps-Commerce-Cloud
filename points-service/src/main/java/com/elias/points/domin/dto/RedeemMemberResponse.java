package com.elias.points.domin.dto;

import lombok.Data;

@Data
public class RedeemMemberResponse {
    private String redeemNo;
    private String targetType;
    private Integer costPoints;
    private Integer balanceAfter;
    private String status;
}
