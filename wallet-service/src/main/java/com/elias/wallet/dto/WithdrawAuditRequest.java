package com.elias.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "WithdrawAuditRequest", description = "提现审核请求")
public class WithdrawAuditRequest {
    private String reason;
}