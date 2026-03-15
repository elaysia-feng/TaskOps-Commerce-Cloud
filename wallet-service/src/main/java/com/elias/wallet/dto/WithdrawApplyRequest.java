package com.elias.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(name = "WithdrawApplyRequest", description = "提现申请请求")
public class WithdrawApplyRequest {
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
    @NotBlank
    private String accountType;
    @NotBlank
    private String accountNo;
    @NotBlank
    private String accountName;
    private String remark;
}