package com.elias.common.mq.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaySuccessEvent {
    private String messageId;
    private String orderNo;
    private String payNo;
    private BigDecimal amount;
}
