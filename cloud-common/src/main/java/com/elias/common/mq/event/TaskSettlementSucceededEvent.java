package com.elias.common.mq.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskSettlementSucceededEvent {
    private String messageId;
    private Long settlementId;
    private Long taskId;
    private Long publisherId;
    private Long acceptorId;
    private String tradeOrderNo;
    private BigDecimal settleAmount;
    private String walletFlowNo;
    private LocalDateTime settledAt;
}