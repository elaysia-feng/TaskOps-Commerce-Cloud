package com.elias.points.mq.config;

import com.elias.common.mq.MqConstants;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.context.annotation.Bean;

public class RabbitMqConfig {
    // 1.交换机
    @Bean
    public DirectExchange pointsExchange() {
        return new DirectExchange(MqConstants.EXCHANGE_BUSINESS, true, false);
    }

    // 2.队列
//    @Bean
////    public Queue businessQueue() {
////        return new Queue(MqConstants.)
//    }
}
