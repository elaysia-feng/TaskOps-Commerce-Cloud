package com.elias.notification.mq.config;

import com.elias.common.mq.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
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
    public Binding taskSubmittedBinding(
            //在spring注册这个的时候,可能会把传递的参数的名称丢掉,比如
//            @Bean
//            public String getUserMessage(String username, String UserId)
            //这个 注册进去 就会变成 getUserMessage(String, String), 所以这里要用Qualifier标记
            @Qualifier("taskSubmittedNotificationReviewQueue") Queue taskSubmittedNotificationReviewQueue,
            DirectExchange businessExchange) {
        return BindingBuilder.bind(taskSubmittedNotificationReviewQueue)
                .to(businessExchange)
                .with(MqConstants.RK_TASK_SUBMITTED);
    }

    @Bean
    public Binding taskRejectedBinding(
            @Qualifier("taskRejectedNotificationReviseQueue") Queue taskRejectedNotificationReviseQueue,
            DirectExchange businessExchange) {
        return BindingBuilder.bind(taskRejectedNotificationReviseQueue)
                .to(businessExchange)
                .with(MqConstants.RK_TASK_REJECTED);
    }
}