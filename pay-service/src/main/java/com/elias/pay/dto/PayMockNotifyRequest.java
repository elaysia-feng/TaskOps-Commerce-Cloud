package com.elias.pay.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PayMockNotifyRequest {
    @NotBlank
    private String orderNo;
}
