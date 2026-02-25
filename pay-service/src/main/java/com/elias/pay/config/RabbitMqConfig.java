package com.elias.pay.config;

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
    public Queue payOrderCreatedQueue() {
        return new Queue(MqConstants.QUEUE_PAY_ORDER_CREATED, true);
    }

    @Bean
    public Binding payOrderCreatedBinding() {
        return BindingBuilder.bind(payOrderCreatedQueue())
                .to(businessExchange())
                .with(MqConstants.RK_ORDER_CREATED);
    }
}
