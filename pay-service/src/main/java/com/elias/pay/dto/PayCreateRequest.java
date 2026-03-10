package com.elias.pay.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PayCreateRequest {
    @NotBlank
    private String orderNo;

    @NotBlank
    private String channel;
}
