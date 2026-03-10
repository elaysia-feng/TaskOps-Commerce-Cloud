package com.elias.notification.mq.config;

import com.elias.common.mq.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public DirectExchange businessExchange() {
        return new DirectExchange(MqConstants.EXCHANGE_BUSINESS, true, false);
    }

    @Bean
    public Queue taskSubmittedNotificationReviewQueue() {
        return new Queue(MqConstants.QUEUE_TASK_SUBMITTED_NOTIFICATION_REVIEW, true);
    }

    @Bean
    public Queue taskRejectedNotificationReviseQueue() {
        return new Queue(MqConstants.QUEUE_TASK_REJECTED_NOTIFICATION_REVISE, true);
    }

    @Bean
    public Binding taskSubmittedBinding(Queue taskSubmittedNotificationReviewQueue,
                                        DirectExchange businessExchange) {
        return BindingBuilder.bind(taskSubmittedNotificationReviewQueue)
                .to(businessExchange)
                .with(MqConstants.RK_TASK_SUBMITTED);
    }

    @Bean
    public Binding taskRejectedBinding(Queue taskRejectedNotificationReviseQueue,
                                       DirectExchange businessExchange) {
        return BindingBuilder.bind(taskRejectedNotificationReviseQueue)
                .to(businessExchange)
                .with(MqConstants.RK_TASK_REJECTED);
    }
}