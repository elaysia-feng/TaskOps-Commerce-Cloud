package com.elias.task.mq.config;

import com.elias.common.mq.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.DirectExchange;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public DirectExchange taskExchange() {
        return new DirectExchange(MqConstants.EXCHANGE_BUSINESS, true, false);
    }

    @Bean
    public Queue taskQueue() {
        return new Queue(MqConstants.QUEUE_PAY_SUCCESS_TASK, true);
    }

    @Bean
    public Binding binding(Queue taskQueue, DirectExchange taskExchange) {
        return BindingBuilder.bind(taskQueue).to(taskExchange).with(MqConstants.RK_PAY_SUCCESS);
    }
}
