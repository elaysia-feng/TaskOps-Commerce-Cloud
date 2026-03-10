package com.elias.notification.mq.consumer;

import com.elias.common.mq.MqConstants;
import com.elias.common.mq.event.TaskRejectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
// 任务拒绝通知消费者
public class TaskRejectedNotificationConsumer {

    @RabbitListener(queues = MqConstants.QUEUE_TASK_REJECTED_NOTIFICATION_REVISE)
    public void onTaskRejected(TaskRejectedEvent event) {
        if (event == null || event.getTaskId() == null || event.getAcceptorId() == null) {
            log.warn("ignore invalid task rejected event: {}", event);
            return;
        }
        log.info("receive task rejected notification event, taskId={}, acceptorId={}, reason={}",
                event.getTaskId(), event.getAcceptorId(), event.getReason());
        // TODO 后续在这里写站内信、待办提醒、短信或邮件通知逻辑。
    }
}