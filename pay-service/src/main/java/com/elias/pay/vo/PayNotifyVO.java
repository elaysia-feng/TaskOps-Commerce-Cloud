package com.elias.pay.vo;

import lombok.Data;

@Data
public class PayNotifyVO {
    private String payNo;
    private String orderNo;
    private String status;
}
