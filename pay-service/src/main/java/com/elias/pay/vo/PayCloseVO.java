package com.elias.pay.vo;

import lombok.Data;

@Data
public class PayCloseVO {
    private String payNo;
    private String orderNo;
    private String status;
}
