package com.elias.pay.mq.config;

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
    public Queue orderCreatedQueue() {
        return new Queue(MqConstants.QUEUE_ORDER_CREATED_PAY_CREATE, true);
    }

    @Bean
    public Binding orderCreatedBinding(
            @Qualifier("orderCreatedQueue") Queue orderCreatedQueue,
            DirectExchange businessExchange) {
        return BindingBuilder.bind(orderCreatedQueue)
                .to(businessExchange)
                .with(MqConstants.RK_ORDER_CREATED);
    }
}