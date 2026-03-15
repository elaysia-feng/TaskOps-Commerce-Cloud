package com.elias.task.mq.config;

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
    public Queue settlementSucceededTaskUpdateQueue() {
        return new Queue(MqConstants.QUEUE_WALLET_SETTLEMENT_SUCCEEDED_TASK_UPDATE, true);
    }

    @Bean
    public Queue settlementFailedTaskUpdateQueue() {
        return new Queue(MqConstants.QUEUE_WALLET_SETTLEMENT_FAILED_TASK_UPDATE, true);
    }

    @Bean
    public Binding settlementSucceededBinding(
            @Qualifier("settlementSucceededTaskUpdateQueue") Queue settlementSucceededTaskUpdateQueue,
            DirectExchange businessExchange) {
        return BindingBuilder.bind(settlementSucceededTaskUpdateQueue)
                .to(businessExchange)
                .with(MqConstants.RK_TASK_SETTLEMENT_SUCCEEDED);
    }

    @Bean
    public Binding settlementFailedBinding(
            @Qualifier("settlementFailedTaskUpdateQueue") Queue settlementFailedTaskUpdateQueue,
            DirectExchange businessExchange) {
        return BindingBuilder.bind(settlementFailedTaskUpdateQueue)
                .to(businessExchange)
                .with(MqConstants.RK_TASK_SETTLEMENT_FAILED);
    }
}