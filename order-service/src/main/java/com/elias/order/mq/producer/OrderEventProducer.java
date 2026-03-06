package com.elias.order.mq.producer;

import com.elias.common.mq.MqConstants;
import com.elias.common.mq.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendOrderCreated(OrderCreatedEvent event) {
        rabbitTemplate.convertAndSend(
                MqConstants.EXCHANGE_BUSINESS,
                MqConstants.RK_ORDER_CREATED,
                event
        );
    }
}
