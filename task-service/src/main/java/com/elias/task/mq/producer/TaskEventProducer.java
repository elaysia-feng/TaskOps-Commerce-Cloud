package com.elias.task.mq.producer;

import com.elias.common.mq.MqConstants;
import com.elias.common.mq.event.TaskRejectedEvent;
import com.elias.common.mq.event.TaskSettlementRequestedEvent;
import com.elias.common.mq.event.TaskSubmittedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskEventProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送“任务已提交”事件。
     * 用途：接单方提交成果后，通知下游服务提醒发布方进行验收。
     */
    public void sendTaskSubmitted(TaskSubmittedEvent event) {
        rabbitTemplate.convertAndSend(
                MqConstants.EXCHANGE_BUSINESS,
                MqConstants.RK_TASK_SUBMITTED,
                event
        );
    }

    /**
     * 发送“任务被驳回”事件。
     * 用途：发布方驳回成果后，通知下游服务提醒接单方修改后重新提交。
     */
    public void sendTaskRejected(TaskRejectedEvent event) {
        rabbitTemplate.convertAndSend(
                MqConstants.EXCHANGE_BUSINESS,
                MqConstants.RK_TASK_REJECTED,
                event
        );
    }

    /**
     * 发送“任务结算请求”事件。
     * 用途：发布方审批通过后，通知钱包服务执行余额结算。
     */
    public void sendTaskSettlementRequested(TaskSettlementRequestedEvent event) {
        rabbitTemplate.convertAndSend(
                MqConstants.EXCHANGE_BUSINESS,
                MqConstants.RK_TASK_SETTLEMENT_REQUESTED,
                event
        );
    }

}