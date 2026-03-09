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
    public Queue paySuccessOrderQueue() {
        return new Queue(MqConstants.QUEUE_PAY_SUCCESS_ORDER, true);
    }

    @Bean
    public Binding paySuccessOrderBinding(Queue paySuccessOrderQueue, DirectExchange businessExchange) {
        return BindingBuilder.bind(paySuccessOrderQueue)
                .to(businessExchange)
                .with(MqConstants.RK_PAY_SUCCESS);
    }
}
