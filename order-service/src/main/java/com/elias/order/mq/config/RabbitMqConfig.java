package com.elias.order.mq.config;

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
    public Queue paySuccessOrderUpdateQueue() {
        return new Queue(MqConstants.QUEUE_PAY_SUCCESS_ORDER_UPDATE, true);
    }

    @Bean
    public Queue payFailOrderUpdateQueue() {
        return new Queue(MqConstants.QUEUE_PAY_FAIL_ORDER_UPDATE, true);
    }

    @Bean
    public Binding paySuccessOrderBinding(Queue paySuccessOrderUpdateQueue, DirectExchange businessExchange) {
        return BindingBuilder.bind(paySuccessOrderUpdateQueue)
                .to(businessExchange)
                .with(MqConstants.RK_PAY_SUCCESS);
    }

    @Bean
    public Binding payFailOrderBinding(Queue payFailOrderUpdateQueue, DirectExchange businessExchange) {
        return BindingBuilder.bind(payFailOrderUpdateQueue)
                .to(businessExchange)
                .with(MqConstants.RK_PAY_FAIL);
    }
}