package com.elias.pay.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PayCallbackRequest {
    @NotBlank
    private String orderNo;
}
