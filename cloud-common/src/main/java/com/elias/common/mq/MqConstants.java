package com.elias.common.mq;

/**
 * MQ 常量定义。
 *
 * 统一约定：
 * 1. 全项目共用一个业务交换机：taskops.business.exchange。
 * 2. 路由键表达“发生了什么事件”。
 * 3. 队列名遵循：<publish.event>.<consumer.event>.queue。
 */
public final class MqConstants {

    private MqConstants() {
    }

    /**
     * 统一业务交换机。
     */
    public static final String EXCHANGE_BUSINESS = "taskops.business.exchange";

    // =========================
    // 订单 / 支付链路
    // =========================

    /**
     * 订单创建事件。
     * order-service 发送，pay-service 消费后预创建支付单。
     */
    public static final String RK_ORDER_CREATED = "order.created";

    /**
     * 支付成功事件。
     * pay-service 发送，order-service 消费后更新订单状态。
     */
    public static final String RK_PAY_SUCCESS = "pay.success";

    /**
     * 支付失败事件。
     * pay-service 发送，order-service 或其他服务可消费后做失败处理。
     */
    public static final String RK_PAY_FAIL = "pay.fail";

    /**
     * 队列：order.created -> pay.create。
     * pay-service 消费“订单已创建”事件。
     */
    public static final String QUEUE_ORDER_CREATED_PAY_CREATE = "order.created.pay.create.queue";

    /**
     * 队列：pay.success -> order.update。
     * order-service 消费“支付成功”事件并更新订单状态。
     */
    public static final String QUEUE_PAY_SUCCESS_ORDER_UPDATE = "pay.success.order.update.queue";

    /**
     * 队列：pay.fail -> order.update。
     * order-service 消费“支付失败”事件并更新订单状态或关闭订单。
     */
    public static final String QUEUE_PAY_FAIL_ORDER_UPDATE = "pay.fail.order.update.queue";

    // =========================
    // 任务接单 / 结算链路
    // =========================

    /**
     * 任务提交事件。
     * task-service 发送，通知服务消费后提醒发布方验收。
     */
    public static final String RK_TASK_SUBMITTED = "task.submitted";

    /**
     * 任务驳回事件。
     * task-service 发送，通知服务消费后提醒接单方重新提交。
     */
    public static final String RK_TASK_REJECTED = "task.rejected";

    /**
     * 任务结算请求事件。
     * task-service 发送，wallet-service 消费后执行余额结算。
     */
    public static final String RK_TASK_SETTLEMENT_REQUESTED = "task.settlement.requested";

    /**
     * 任务结算成功事件。
     * wallet-service 发送，task-service 消费后将任务状态改为 SETTLED。
     */
    public static final String RK_TASK_SETTLEMENT_SUCCEEDED = "task.settlement.succeeded";

    /**
     * 任务结算失败事件。
     * wallet-service 发送，task-service 消费后记录失败状态并决定是否重试。
     */
    public static final String RK_TASK_SETTLEMENT_FAILED = "task.settlement.failed";

    /**
     * 队列：task.submitted -> notification.review。
     * 通知服务消费“任务提交”事件，提醒发布方有待验收任务。
     */
    public static final String QUEUE_TASK_SUBMITTED_NOTIFICATION_REVIEW = "task.submitted.notification.review.queue";

    /**
     * 队列：task.rejected -> notification.revise。
     * 通知服务消费“任务驳回”事件，提醒接单方修改后重新提交。
     */
    public static final String QUEUE_TASK_REJECTED_NOTIFICATION_REVISE = "task.rejected.notification.revise.queue";

    /**
     * 队列：task.settlement.requested -> wallet.settle。
     * wallet-service 消费“任务结算请求”事件，执行余额入账。
     */
    public static final String QUEUE_TASK_SETTLEMENT_REQUESTED_WALLET_SETTLE = "task.settlement.requested.wallet.settle.queue";

    /**
     * 队列：wallet.settlement.succeeded -> task.update。
     * task-service 消费“任务结算成功”事件，回写任务最终状态。
     */
    public static final String QUEUE_WALLET_SETTLEMENT_SUCCEEDED_TASK_UPDATE = "wallet.settlement.succeeded.task.update.queue";

    /**
     * 队列：wallet.settlement.failed -> task.update。
     * task-service 消费“任务结算失败”事件，记录失败信息并决定是否补偿。
     */
    public static final String QUEUE_WALLET_SETTLEMENT_FAILED_TASK_UPDATE = "wallet.settlement.failed.task.update.queue";
}