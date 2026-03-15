package com.elias.wallet.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WithdrawRecordVO {
    private Long id;
    private String withdrawNo;
    private BigDecimal amount;
    private String accountType;
    private String accountNo;
    private String accountName;
    private String status;
    private String remark;
    private String rejectReason;
    private LocalDateTime auditAt;
    private LocalDateTime createdAt;
}