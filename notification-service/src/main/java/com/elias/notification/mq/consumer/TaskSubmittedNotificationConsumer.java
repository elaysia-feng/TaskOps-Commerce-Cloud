package com.elias.notification.mq.consumer;

import com.elias.common.mq.MqConstants;
import com.elias.common.mq.event.TaskSubmittedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
//任务提交通知消费者
public class TaskSubmittedNotificationConsumer {

    @RabbitListener(queues = MqConstants.QUEUE_TASK_SUBMITTED_NOTIFICATION_REVIEW)
    public void onTaskSubmitted(TaskSubmittedEvent event) {
        if (event == null || event.getTaskId() == null || event.getPublisherId() == null) {
            log.warn("ignore invalid task submitted event: {}", event);
            return;
        }
        log.info("接单成功 taskId={}, publisherId={}, submissionId={}",
                event.getTaskId(), event.getPublisherId(), event.getSubmissionId());
        // TODO 后续在这里写站内信、待办提醒、短信或邮件通知逻辑。
    }
}