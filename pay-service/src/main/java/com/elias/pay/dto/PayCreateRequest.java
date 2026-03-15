package com.elias.pay.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class PayCreateRequest {
    @NotBlank
    private String orderNo;

    @NotBlank
    private String channel;
}
