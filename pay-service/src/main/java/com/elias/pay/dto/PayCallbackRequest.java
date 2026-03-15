package com.elias.pay.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class PayCallbackRequest {
    @NotBlank
    private String orderNo;
}
