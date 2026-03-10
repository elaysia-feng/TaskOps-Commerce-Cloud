package com.elias.common.mq;

public final class MqConstants {

    private MqConstants() {
    }

    public static final String EXCHANGE_BUSINESS = "taskops.business.exchange";

    public static final String RK_TASK_CREATE = "task.create";
    public static final String RK_ORDER_CREATED = "order.created";
    public static final String RK_PAY_SUCCESS = "pay.success";
    public static final String RK_PAY_FAIL = "pay.fail";


    public static final String QUEUE_ORDER_CREATED_PAY = "order.created.pay.queue";
    public static final String QUEUE_PAY_SUCCESS_ORDER = "pay.success.order.queue";
    public static final String QUEUE_PAY_SUCCESS_TASK = "pay.success.task.queue";
}
