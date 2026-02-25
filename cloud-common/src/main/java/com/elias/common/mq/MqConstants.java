package com.elias.common.mq;

public final class MqConstants {

    private MqConstants() {
    }

    public static final String EXCHANGE_BUSINESS = "taskops.business.exchange";

    public static final String RK_ORDER_CREATED = "order.created";
    public static final String RK_PAY_SUCCESS = "pay.success";

    public static final String QUEUE_PAY_ORDER_CREATED = "pay.order.created.queue";
    public static final String QUEUE_ORDER_PAY_SUCCESS = "order.pay.success.queue";
}
